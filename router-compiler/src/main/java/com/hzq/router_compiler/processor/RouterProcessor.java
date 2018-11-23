package com.hzq.router_compiler.processor;

import com.google.auto.service.AutoService;
import com.hzq.router_annotation.facade.annotation.Autowired;
import com.hzq.router_annotation.facade.annotation.Route;
import com.hzq.router_annotation.facade.enums.RouteType;
import com.hzq.router_annotation.facade.model.RouteMeta;
import com.hzq.router_compiler.utils.Consts;
import com.hzq.router_compiler.utils.Logger;
import com.hzq.router_compiler.utils.TypeUtils;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.WildcardTypeName;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedOptions;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import static com.hzq.router_compiler.utils.Consts.ACTIVITY;
import static com.hzq.router_compiler.utils.Consts.ANNOTATION_ROUTER_NAME;
import static com.hzq.router_compiler.utils.Consts.FRAGMENT;
import static com.hzq.router_compiler.utils.Consts.FRAGMENT_V4;
import static com.hzq.router_compiler.utils.Consts.IPROVIDER;
import static com.hzq.router_compiler.utils.Consts.IPROVIDER_GROUP;
import static com.hzq.router_compiler.utils.Consts.IROUTE_GROUP;
import static com.hzq.router_compiler.utils.Consts.IROUTE_ROOT;
import static com.hzq.router_compiler.utils.Consts.METHOD_LOAD_INTO;
import static com.hzq.router_compiler.utils.Consts.NAME_OF_GROUP;
import static com.hzq.router_compiler.utils.Consts.NAME_OF_PROVIDER;
import static com.hzq.router_compiler.utils.Consts.NAME_OF_ROOT;
import static com.hzq.router_compiler.utils.Consts.PACKAGE_OF_GENERATE_FILE;
import static com.hzq.router_compiler.utils.Consts.WARNING_TIPS;
import static javax.lang.model.element.Modifier.PUBLIC;

/**
 * Created by hezhiqiang on 2018/11/21.
 * Router注解处理器
 */

@AutoService(Processor.class)       //自动注册注解处理器
@SupportedOptions({Consts.KEY_MODULE_NAME})     //参数
@SupportedSourceVersion(SourceVersion.RELEASE_7)        //指定使用的Java版本
@SupportedAnnotationTypes({ANNOTATION_ROUTER_NAME}) //指定要处理的注解类型
public class RouterProcessor extends AbstractProcessor{

    private Map<String,Set<RouteMeta>> groupMap = new HashMap<>();  //收集分组
    private Map<String,String> rootMap = new TreeMap<>();
    private Filer mFiler;
    private Logger logger;
    private Types types;
    private TypeUtils typeUtils;
    private Elements elements;
    private String moduleName = "app"; //默认app
    private TypeMirror iProvider = null; //IProvider类型

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        mFiler = processingEnvironment.getFiler();
        types = processingEnvironment.getTypeUtils();
        elements = processingEnvironment.getElementUtils();

        typeUtils = new TypeUtils(types,elements);
        logger = new Logger(processingEnvironment.getMessager()); //log工具
        Map<String, String> options = processingEnvironment.getOptions();
        if(MapUtils.isNotEmpty(options)) {
            moduleName = options.get(Consts.KEY_MODULE_NAME);
        }

        iProvider = elements.getTypeElement(IPROVIDER).asType();

        logger.info(">>> RouterProcessor init. <<<");
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        if(CollectionUtils.isNotEmpty(set)) {
            Set<? extends Element> elementsAnnotatedWith = roundEnvironment.getElementsAnnotatedWith(Route.class);

            try {
                logger.info(">>> Found routers,start... <<<");
                parseRoutes(elementsAnnotatedWith);
            } catch (IOException e) {
                logger.error(e);
            }
            return true;
        }
        return false;
    }

    private void parseRoutes(Set<? extends Element> routeElements) throws IOException {
        if(CollectionUtils.isNotEmpty(routeElements)) {

            logger.info(">>> Found routes, size is " + routeElements.size() + " <<<");
            rootMap.clear();

            TypeMirror type_activity = elements.getTypeElement(ACTIVITY).asType();
            TypeMirror type_fragment = elements.getTypeElement(FRAGMENT).asType();
            TypeMirror type_v4_fragment = elements.getTypeElement(FRAGMENT_V4).asType();

            //interface of route
            TypeElement type_IRouteGroup = elements.getTypeElement(IROUTE_GROUP);
            TypeElement type_IProvider_group = elements.getTypeElement(IPROVIDER_GROUP);

            ClassName routeMetaCn = ClassName.get(RouteMeta.class);
            ClassName routeTypeCn = ClassName.get(RouteType.class);

            /**
             * Build input type, format as :
             * ``` Map<String,Class<? extends IRouteGroup>>```
             */
            ParameterizedTypeName inputMapTyppeOfRoot = ParameterizedTypeName.get(
                    ClassName.get(Map.class),
                    ClassName.get(String.class),
                    ParameterizedTypeName.get(ClassName.get(Class.class),
                            WildcardTypeName.subtypeOf(ClassName.get(type_IRouteGroup))));

            /**
             * Map<String,RouteMeta></ 构造参数类型
             */
            ParameterizedTypeName inputMapTypeOfGroup = ParameterizedTypeName.get(
                    ClassName.get(Map.class)
                    ,ClassName.get(String.class)
                    ,ClassName.get(RouteMeta.class));


            /**
             * Build input params name. 构造参数名称
             */
            ParameterSpec rootParamSpec = ParameterSpec.builder(inputMapTyppeOfRoot, "routes").build();
            ParameterSpec groupParamSpec = ParameterSpec.builder(inputMapTypeOfGroup,"atlas").build();
            ParameterSpec providerParamsSpec = ParameterSpec.builder(inputMapTypeOfGroup,"providers").build();

            /*
              Build method : 'loadInto'
             */
            MethodSpec.Builder loadIntoMethodOfRootBuilder = MethodSpec.methodBuilder(METHOD_LOAD_INTO)
                    .addAnnotation(Override.class)
                    .addModifiers(PUBLIC)
                    .addParameter(rootParamSpec);

            for (Element element : routeElements) {
                TypeMirror tm = element.asType();
                Route route = element.getAnnotation(Route.class);
                RouteMeta routeMeta;

                if(types.isSubtype(tm,type_activity)) { //activity
                    logger.info(">>> Found activity route: "+ tm.toString() + " <<<");
                    routeMeta = new RouteMeta(route,element,RouteType.ACTIVITY,null);
                } else if(types.isSubtype(tm,iProvider)) { //IProvider
                    logger.info(">>> Found provider route: " + tm.toString() + " <<<");
                    routeMeta = new RouteMeta(route,element,RouteType.PROVIDER,null);
                } else if(types.isSubtype(tm,type_fragment) || types.isSubtype(tm,type_v4_fragment)) { //Fragment
                    logger.info(">>> Found fragment route: " + tm.toString() + " <<< ");
                    routeMeta = new RouteMeta(route,element,RouteType.parse(FRAGMENT),null);
                } else {
                    throw new RuntimeException("ARouter::Compiler >>> Found unsupported class type, type = [" + types.toString() + "].");
                }

                categories(routeMeta);
            }

            /**
             * public void loadinfo(Map<String,RouteMeta>> providers){}
             */
            MethodSpec.Builder loadIntoMethodOfProviderBuilder = MethodSpec.methodBuilder(METHOD_LOAD_INTO)
                    .addAnnotation(Override.class)
                    .addModifiers(Modifier.PUBLIC)
                    .addParameter(providerParamsSpec);

            for (Map.Entry<String, Set<RouteMeta>> entry : groupMap.entrySet()) {
                String groupName = entry.getKey();

                MethodSpec.Builder loadIntoMethodOfGroupBuilder = MethodSpec.methodBuilder(METHOD_LOAD_INTO)
                        .addAnnotation(Override.class)
                        .addModifiers(Modifier.PUBLIC)
                        .addParameter(groupParamSpec);

                Set<RouteMeta> groupData = entry.getValue();
                for (RouteMeta meta : groupData) {
                    ClassName className = ClassName.get((TypeElement) meta.getRawType());
                    switch (meta.getType()) {
                        case PROVIDER:
                            List<? extends TypeMirror> interfaces = ((TypeElement)meta.getRawType()).getInterfaces();
                            for (TypeMirror typeMirror : interfaces) {
                                if(types.isSameType(typeMirror,iProvider)) {
                                    //this interface extends the IProvider, so it can be used for mark provider
                                    loadIntoMethodOfProviderBuilder.addStatement(
                                            "providers.put($S," +
                                                    "$T.build($T." + meta.getType() + ",$T.class,$S,$S,null, " + meta.getPriority() + "," + meta.getExtra() + "))",
                                            (meta.getRawType()).toString(),
                                            routeMetaCn,
                                            routeTypeCn,
                                            className,
                                            meta.getPath(),
                                            meta.getGroup());

                                } else if(types.isSubtype(typeMirror,iProvider)) {
                                    // This interface extend the IProvider, so it can be used for mark provider
                                    loadIntoMethodOfProviderBuilder.addStatement(
                                            "providers.put($S, $T.build($T." + meta.getType() + ", $T.class, $S, $S, null, " + meta.getPriority() + ", " + meta.getExtra() + "))",
                                            typeMirror.toString(),    // So stupid, will duplicate only save class name.
                                            routeMetaCn,
                                            routeTypeCn,
                                            className,
                                            meta.getPath(),
                                            meta.getGroup());
                                }
                            }
                            break;
                        default:
                            break;
                    }

                    // make map body for paramsType
                    StringBuilder mapBodyBuilder = new StringBuilder();
                    Map<String,Integer> paramsType = meta.getParamsType();
                    if(MapUtils.isNotEmpty(paramsType)) {
                        for (Map.Entry<String, Integer> integerEntry : paramsType.entrySet()) {
                            mapBodyBuilder.append("put(\"").append(integerEntry.getKey()).append("\",").append(integerEntry.getValue()).append("); ");
                        }
                    }

                    String mapBody = mapBodyBuilder.toString();

                    loadIntoMethodOfGroupBuilder.addStatement(
                            "atlas.put($S," +
                                    "$T.build($T." + meta.getType() + ",$T.class,$S,$S," + (StringUtils.isEmpty(mapBody) ? null : ("new java.util.HashMap<String, Integer>(){{" + mapBodyBuilder.toString() + "}}")) + ", " + meta.getPriority() + "," + meta.getExtra() + "))",
                            meta.getPath(),
                            routeMetaCn,
                            routeTypeCn,
                            className,
                            meta.getPath().toLowerCase(),
                            meta.getGroup().toLowerCase());
                }

                //Generate groups
                String groupFileName = NAME_OF_GROUP + groupName;
                JavaFile.builder(PACKAGE_OF_GENERATE_FILE,
                        TypeSpec.classBuilder(groupFileName)
                                .addJavadoc(WARNING_TIPS)
                                .addSuperinterface(ClassName.get(type_IRouteGroup))
                                .addModifiers(Modifier.PUBLIC)
                                .addMethod(loadIntoMethodOfGroupBuilder.build())
                                .build()
                ).build().writeTo(mFiler);

                logger.info(">>> Generated group: " + groupName + "<<<");
                rootMap.put(groupName, groupFileName);
            }

            if(MapUtils.isNotEmpty(rootMap)) {
                for (Map.Entry<String, String> entry : rootMap.entrySet()) {
                    loadIntoMethodOfRootBuilder.addStatement("routes.put($S, $T.class)", entry.getKey(), ClassName.get(PACKAGE_OF_GENERATE_FILE, entry.getValue()));
                }
            }

            // Write provider into disk
            String providerMapFileName = NAME_OF_PROVIDER + moduleName;
            JavaFile.builder(PACKAGE_OF_GENERATE_FILE,
                    TypeSpec.classBuilder(providerMapFileName)
                            .addJavadoc(WARNING_TIPS)
                            .addSuperinterface(ClassName.get(type_IProvider_group))
                            .addModifiers(PUBLIC)
                            .addMethod(loadIntoMethodOfProviderBuilder.build())
                            .build())
                    .build().writeTo(mFiler);

            logger.info(">>> Generated provider map, name is " + providerMapFileName + " <<<");

            // Write root meta into disk.
            String rootFileName = NAME_OF_ROOT + moduleName;
            JavaFile.builder(PACKAGE_OF_GENERATE_FILE,
                    TypeSpec.classBuilder(rootFileName)
                            .addJavadoc(WARNING_TIPS)
                            .addSuperinterface(ClassName.get(elements.getTypeElement(IROUTE_ROOT)))
                            .addModifiers(PUBLIC)
                            .addMethod(loadIntoMethodOfRootBuilder.build())
                            .build()
            ).build().writeTo(mFiler);

            logger.info(">>> Generated root, name is " + rootFileName + " <<<");
        }
    }

    /**
     * Sort metas in group.
     *
     * @param routeMete metas.
     */
    private void categories(RouteMeta routeMete) {
        if (routeVerify(routeMete)) {
            logger.info(">>> Start categories, group = " + routeMete.getGroup() + ", path = " + routeMete.getPath() + " <<<");
            Set<RouteMeta> routeMetas = groupMap.get(routeMete.getGroup());
            if (CollectionUtils.isEmpty(routeMetas)) {
                Set<RouteMeta> routeMetaSet = new TreeSet<>(new Comparator<RouteMeta>() {
                    @Override
                    public int compare(RouteMeta r1, RouteMeta r2) {
                        try {
                            return r1.getPath().compareTo(r2.getPath());
                        } catch (NullPointerException npe) {
                            logger.error(npe.getMessage());
                            return 0;
                        }
                    }
                });
                routeMetaSet.add(routeMete);
                groupMap.put(routeMete.getGroup(), routeMetaSet);
            } else {
                routeMetas.add(routeMete);
            }
        } else {
            logger.warning(">>> Route meta verify error, group is " + routeMete.getGroup() + " <<<");
        }
    }

    /**
     * Verify the route meta
     *
     * @param meta raw meta
     */
    private boolean routeVerify(RouteMeta meta) {
        String path = meta.getPath();

        if (StringUtils.isEmpty(path) || !path.startsWith("/")) {   // The path must be start with '/' and not empty!
            return false;
        }

        if (StringUtils.isEmpty(meta.getGroup())) { // Use default group(the first word in path)
            try {
                String defaultGroup = path.substring(1, path.indexOf("/", 1));
                if (StringUtils.isEmpty(defaultGroup)) {
                    return false;
                }

                meta.setGroup(defaultGroup);
                return true;
            } catch (Exception e) {
                logger.error("Failed to extract default group! " + e.getMessage());
                return false;
            }
        }

        return true;
    }
}
