package net.sunshow.toolkit.core.qbean.api.annotation.processor;

import com.squareup.javapoet.*;
import net.sunshow.toolkit.core.qbean.api.annotation.*;
import net.sunshow.toolkit.core.qbean.api.bean.BaseQBeanCreator;
import net.sunshow.toolkit.core.qbean.api.bean.BaseQBeanUpdater;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import java.util.*;

/**
 * 实际的QBean注解处理器
 * Created by sunshow.
 */
public class QBeanProcessor extends AbstractProcessor {

    private Types typeUtils;
    private Elements elementUtils;
    private Filer filer;
    private Messager messager;

    private final static String FIELD_UPDATER = "updater";
    private final static String FIELD_UPDATE_ID = "updateId";
    private final static String FIELD_UPDATE_PROPERTIES = "updateProperties";
    private final static String TYPE_BUILDER = "Builder";
    private final static String TYPE_QUERY_PREFIX = "Q";
    private final static String TYPE_UPDATER_SUFFIX = "Updater";
    private final static String METHOD_BUILD = "build";
    private final static String METHOD_BUILDER = "builder";
    private final static String FIELD_CREATE_PROPERTIES = "createProperties";
    private final static String FIELD_CREATOR = "creator";
    private final static String TYPE_CREATOR_SUFFIX = "Creator";

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        System.err.println("QBeanProcessor Run");
        super.init(processingEnvironment);

        elementUtils = processingEnvironment.getElementUtils();
        filer = processingEnvironment.getFiler();
        typeUtils = processingEnvironment.getTypeUtils();
        messager = processingEnvironment.getMessager();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

        for (Element em : roundEnv.getElementsAnnotatedWith(QBean.class)) {
            if (!em.getKind().equals(ElementKind.CLASS)) {
                error(em, "错误的注解类型, 只有类对象能够被该 @%s 注解处理", QBean.class.getSimpleName());
                return true;
            }

            TypeElement typeElement = (TypeElement) em;

            System.err.println("QBeanProcessor Process: " + typeElement.getQualifiedName());

            PackageElement packageElement = elementUtils.getPackageOf(typeElement);

            this.generateQBeanQueryType(typeElement, packageElement);
            this.generateQBeanCreator(typeElement, packageElement);
            this.generateQBeanUpdater(typeElement, packageElement);
        }

        return false;
    }

    /**
     * 生成用于属性名称查询的类
     *
     * @param typeElement    标记注解的类
     * @param packageElement 标记注解的类所在的包
     */
    private void generateQBeanQueryType(TypeElement typeElement, PackageElement packageElement) {
        // 开始组装类描述
        TypeSpec.Builder typeSpecBuilder = TypeSpec.classBuilder(TYPE_QUERY_PREFIX + typeElement.getSimpleName())
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL);

        boolean hasQBeanId = false;

        for (VariableElement variableElement : ElementFilter.fieldsIn(typeElement.getEnclosedElements())) {
            if ((variableElement.getModifiers().contains(Modifier.PRIVATE) || variableElement.getModifiers().contains(Modifier.PROTECTED))
                    && !variableElement.getModifiers().contains(Modifier.FINAL)
                    && !variableElement.getModifiers().contains(Modifier.STATIC)) {

                // 先判断是否ID属性
                if (variableElement.getAnnotation(QBeanID.class) != null) {
                    // 找到ID, 仅标记 不做额外处理
                    hasQBeanId = true;
                }

                String fieldName = variableElement.getSimpleName().toString();

                // 只生成private的非final且非static修饰的属性
                typeSpecBuilder.addField(FieldSpec.builder(String.class,
                                fieldName,
                                Modifier.PUBLIC, Modifier.FINAL, Modifier.STATIC)
                        .initializer("$S", fieldName)
                        .build());
            }
        }

        if (!hasQBeanId) {
            // 尝试解析类注解中的默认ID属性
            QBean annotation = typeElement.getAnnotation(QBean.class);
            if (annotation.defaultIdProperty() && !annotation.defaultIdPropertyCreatorIgnore()) {
                // 说明要创建默认 id 属性
                typeSpecBuilder.addField(FieldSpec.builder(String.class,
                                annotation.defaultIdPropertyName(),
                                Modifier.PUBLIC, Modifier.FINAL, Modifier.STATIC)
                        .initializer("$S", annotation.defaultIdPropertyName())
                        .build());
            }
        }

        TypeSpec typeSpec = typeSpecBuilder.build();

        JavaFile javaFile = JavaFile.builder(packageElement.getQualifiedName().toString(), typeSpec).skipJavaLangImports(true).build();

        try {
            javaFile.writeTo(filer);
        } catch (Exception e) {
            System.err.println("QBeanProcessor Write File Error");
            e.printStackTrace();
        }
    }

    /**
     * 生成用于属性创建的类
     *
     * @param typeElement    标记注解的类
     * @param packageElement 标记注解的类所在的包
     */
    private void generateQBeanCreator(TypeElement typeElement, PackageElement packageElement) {
        QBeanCreator creatorAnnotation = typeElement.getAnnotation(QBeanCreator.class);

        boolean generateCreator = false;

        if (creatorAnnotation != null) {
            generateCreator = true;
        }

        boolean hasQBeanId = false;

        List<VariableElement> createFieldElementList = new ArrayList<>();
        for (VariableElement variableElement : ElementFilter.fieldsIn(typeElement.getEnclosedElements())) {
            if ((variableElement.getModifiers().contains(Modifier.PRIVATE) || variableElement.getModifiers().contains(Modifier.PROTECTED))
                    && !variableElement.getModifiers().contains(Modifier.FINAL)
                    && !variableElement.getModifiers().contains(Modifier.STATIC)) {

                // 是否忽略, 忽略的直接跳过
                if (variableElement.getAnnotation(QBeanCreatorIgnore.class) != null) {
                    continue;
                }

                // 先判断是否ID属性
                if (variableElement.getAnnotation(QBeanID.class) != null) {
                    // 找到ID, 仅标记 不做额外处理
                    hasQBeanId = true;
                }

                if (creatorAnnotation != null) {
                    // 说明每个Field都要允许更新
                    createFieldElementList.add(variableElement);
                } else {
                    if (variableElement.getAnnotation(QBeanCreator.class) != null) {
                        createFieldElementList.add(variableElement);
                        generateCreator = true;
                    }
                }
            }
        }

        if (!generateCreator) {
            // 不生成代码
            return;
        }

        String extraIdProperty = null;
        TypeName extraIdPropertyType = null;

        if (!hasQBeanId) {
            // 尝试解析类注解中的默认ID属性
            QBean annotation = typeElement.getAnnotation(QBean.class);
            if (annotation.defaultIdProperty() && !annotation.defaultIdPropertyCreatorIgnore()) {
                // 说明要创建默认 id 属性
                extraIdProperty = annotation.defaultIdPropertyName();
                try {
                    Class<?> propertyType = annotation.defaultIdPropertyType();
                    extraIdPropertyType = TypeName.get(propertyType);
                } catch (MirroredTypeException e) {
                    extraIdPropertyType = TypeName.get(e.getTypeMirror());
                }
                // System.err.println("QBeanProcessor: use default id property " + typeElement.getQualifiedName() + " with type " + extraIdPropertyType);
            }
        }

        String creatorClassSimpleName = typeElement.getSimpleName() + TYPE_CREATOR_SUFFIX;
        ClassName creatorClassName = ClassName.get(packageElement.getQualifiedName().toString(), creatorClassSimpleName);
        // 开始组装类
        TypeSpec.Builder typeSpecBuilder = TypeSpec.classBuilder(creatorClassSimpleName)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addSuperinterface(BaseQBeanCreator.class);

        ClassName builderClassName = creatorClassName.nestedClass(TYPE_BUILDER);
        // 内部 Builder 类
        TypeSpec.Builder buildTypeSpecBuilder = TypeSpec.classBuilder(TYPE_BUILDER)
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC);

        // 内部类添加外部类属性
        {
            FieldSpec field = FieldSpec.builder(creatorClassName, FIELD_CREATOR)
                    .addModifiers(Modifier.PRIVATE)
                    .build();
            buildTypeSpecBuilder.addField(field);
        }

        // 内部类添加构造函数
        {
            MethodSpec constructor = MethodSpec.constructorBuilder()
                    .addModifiers(Modifier.PRIVATE)
                    .addStatement("this.$N = new $T()", FIELD_CREATOR, creatorClassName)
                    .build();
            buildTypeSpecBuilder.addMethod(constructor);
        }

        // 声明createProperties
        {
            ClassName string = ClassName.get(String.class);
            TypeName setOfString = ParameterizedTypeName.get(ClassName.get(Set.class), string);

            FieldSpec field = FieldSpec.builder(setOfString, FIELD_CREATE_PROPERTIES)
                    .addModifiers(Modifier.PRIVATE)
                    .initializer("new $T<>()", HashSet.class)
                    .build();
            typeSpecBuilder.addField(field);
            // 同时声明 getter
            typeSpecBuilder.addMethod(
                    createGetterBuilder(setOfString, FIELD_CREATE_PROPERTIES, Modifier.PUBLIC)
                            .addAnnotation(Override.class).build()
            );
        }

        // 声明创建属性
        if (extraIdProperty != null) {
            FieldSpec field = FieldSpec.builder(extraIdPropertyType, extraIdProperty)
                    .addModifiers(Modifier.PRIVATE)
                    .build();
            typeSpecBuilder.addField(field);

            // 同时声明 getter
            typeSpecBuilder.addMethod(createGetter(extraIdPropertyType, extraIdProperty, Modifier.PUBLIC));

            // 内部类声明修改创建属性的方法
            MethodSpec createMethod = MethodSpec.methodBuilder("with" + toCamelCase(extraIdProperty))
                    .returns(builderClassName)
                    .addModifiers(Modifier.PUBLIC)
                    .addParameter(extraIdPropertyType, extraIdProperty)
                    .addStatement("this.$N.$N = $N", FIELD_CREATOR, extraIdProperty, extraIdProperty)
                    .addStatement("this.$N.$N.add($S)", FIELD_CREATOR, FIELD_CREATE_PROPERTIES, extraIdProperty)
                    .addStatement("return this")
                    .build();
            buildTypeSpecBuilder.addMethod(createMethod);
        }
        for (VariableElement variableElement : createFieldElementList) {
            TypeName typeName = TypeName.get(variableElement.asType());
            String fieldName = variableElement.getSimpleName().toString();
            FieldSpec field = FieldSpec.builder(typeName, fieldName)
                    .addModifiers(Modifier.PRIVATE)
                    .build();
            typeSpecBuilder.addField(field);

            // 同时声明 getter
            typeSpecBuilder.addMethod(createGetter(typeName, fieldName, Modifier.PUBLIC));

            // 内部类声明修改创建属性的方法
            MethodSpec createMethod = MethodSpec.methodBuilder("with" + toCamelCase(fieldName))
                    .returns(builderClassName)
                    .addModifiers(Modifier.PUBLIC)
                    .addParameter(typeName, fieldName)
                    .addStatement("this.$N.$N = $N", FIELD_CREATOR, fieldName, fieldName)
                    .addStatement("this.$N.$N.add($S)", FIELD_CREATOR, FIELD_CREATE_PROPERTIES, fieldName)
                    .addStatement("return this")
                    .build();
            buildTypeSpecBuilder.addMethod(createMethod);
        }

        // 声明构造函数
        {
            MethodSpec constructor = MethodSpec.constructorBuilder()
                    .addModifiers(Modifier.PRIVATE)
                    .build();
            typeSpecBuilder.addMethod(constructor);
        }

        // 内部类生成 build 方法
        {
            MethodSpec buildMethod = MethodSpec.methodBuilder(METHOD_BUILD)
                    .addModifiers(Modifier.PUBLIC)
                    .returns(creatorClassName)
                    .addStatement("return this.$N", FIELD_CREATOR)
                    .build();
            buildTypeSpecBuilder.addMethod(buildMethod);
        }

        // 外部类创建 builder 方法
        {
            MethodSpec builderMethod = MethodSpec.methodBuilder(METHOD_BUILDER)
                    .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                    .returns(builderClassName)
                    .addStatement("return new $T()", builderClassName)
                    .build();
            typeSpecBuilder.addMethod(builderMethod);
        }

        TypeSpec builderTypeSpec = buildTypeSpecBuilder.build();

        typeSpecBuilder.addType(builderTypeSpec);

        TypeSpec typeSpec = typeSpecBuilder.build();

        JavaFile javaFile = JavaFile.builder(packageElement.getQualifiedName().toString(), typeSpec).skipJavaLangImports(true).build();

        try {
            javaFile.writeTo(filer);
        } catch (Exception e) {
            System.err.println("QBeanProcessor Write File Error");
            e.printStackTrace();
        }
    }

    /**
     * 生成用于属性更新的类
     *
     * @param typeElement    标记注解的类
     * @param packageElement 标记注解的类所在的包
     */
    private void generateQBeanUpdater(TypeElement typeElement, PackageElement packageElement) {
        QBeanUpdater updaterAnnotation = typeElement.getAnnotation(QBeanUpdater.class);

        boolean generateUpdater = false;

        if (updaterAnnotation != null) {
            generateUpdater = true;
        }


        VariableElement updateIdFieldElement = null;
        List<VariableElement> updateFieldElementList = new ArrayList<>();
        for (VariableElement variableElement : ElementFilter.fieldsIn(typeElement.getEnclosedElements())) {
            if ((variableElement.getModifiers().contains(Modifier.PRIVATE) || variableElement.getModifiers().contains(Modifier.PROTECTED))
                    && !variableElement.getModifiers().contains(Modifier.FINAL)
                    && !variableElement.getModifiers().contains(Modifier.STATIC)) {

                // 先判断是否ID属性
                if (variableElement.getAnnotation(QBeanID.class) != null) {
                    // 找到ID
                    updateIdFieldElement = variableElement;
                    continue;
                }

                // 是否忽略, 忽略的直接跳过
                if (variableElement.getAnnotation(QBeanUpdaterIgnore.class) != null) {
                    continue;
                }

                if (updaterAnnotation != null) {
                    // 说明每个Field都要允许更新
                    updateFieldElementList.add(variableElement);
                } else {
                    if (variableElement.getAnnotation(QBeanUpdater.class) != null) {
                        updateFieldElementList.add(variableElement);
                        generateUpdater = true;
                    }
                }
            }
        }

        // 解析ID属性类型
        TypeName updateIdTypeName = null;

        if (updateIdFieldElement == null) {
            System.err.println("QBeanProcessor: cannot find @QBeanID field while processing " + typeElement.getQualifiedName());

            // 尝试解析类注解中的默认ID属性
            QBean annotation = typeElement.getAnnotation(QBean.class);
            if (annotation.defaultIdProperty()) {
                try {
                    annotation.defaultIdPropertyType();
                } catch (MirroredTypeException e) {
                    updateIdTypeName = TypeName.get(e.getTypeMirror());
                }
                System.err.println("QBeanProcessor: use default id property " + typeElement.getQualifiedName() + " with type " + updateIdTypeName);
            }
        } else {
            updateIdTypeName = TypeName.get(updateIdFieldElement.asType());
        }

        if (updateIdTypeName == null) {
            System.err.println("QBeanProcessor: cannot find id property, stop generating updater of " + typeElement.getQualifiedName());
        }

        if (!generateUpdater) {
            // 不生成代码
            return;
        }

        String updaterClassSimpleName = typeElement.getSimpleName() + TYPE_UPDATER_SUFFIX;
        ClassName updaterClassName = ClassName.get(packageElement.getQualifiedName().toString(), updaterClassSimpleName);
        // 开始组装类
        TypeSpec.Builder typeSpecBuilder = TypeSpec.classBuilder(updaterClassSimpleName)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addSuperinterface(BaseQBeanUpdater.class);

        ClassName builderClassName = updaterClassName.nestedClass(TYPE_BUILDER);
        // 内部 Builder 类
        TypeSpec.Builder buildTypeSpecBuilder = TypeSpec.classBuilder(TYPE_BUILDER)
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC);

        // 内部类添加外部类属性
        {
            FieldSpec field = FieldSpec.builder(updaterClassName, FIELD_UPDATER)
                    .addModifiers(Modifier.PRIVATE)
                    .build();
            buildTypeSpecBuilder.addField(field);
        }

        // 声明updateId
        {
            FieldSpec field = FieldSpec.builder(updateIdTypeName, FIELD_UPDATE_ID)
                    .addModifiers(Modifier.PRIVATE)
                    .build();
            typeSpecBuilder.addField(field);
            // 同时声明 getter
            typeSpecBuilder.addMethod(
                    createGetterBuilder(updateIdTypeName, FIELD_UPDATE_ID, Modifier.PUBLIC)
                            .addAnnotation(Override.class).build()
            );
        }

        // 内部类添加构造函数
        {
            MethodSpec constructor = MethodSpec.constructorBuilder()
                    .addModifiers(Modifier.PRIVATE)
                    .addParameter(updateIdTypeName, FIELD_UPDATE_ID)
                    .addStatement("this.$N = new $T($N)", FIELD_UPDATER, updaterClassName, FIELD_UPDATE_ID)
                    .build();
            buildTypeSpecBuilder.addMethod(constructor);
        }

        // 声明updateProperties
        {
            ClassName string = ClassName.get(String.class);
            TypeName setOfString = ParameterizedTypeName.get(ClassName.get(Set.class), string);

            FieldSpec field = FieldSpec.builder(setOfString, FIELD_UPDATE_PROPERTIES)
                    .addModifiers(Modifier.PRIVATE)
                    .initializer("new $T<>()", HashSet.class)
                    .build();
            typeSpecBuilder.addField(field);
            // 同时声明 getter
            typeSpecBuilder.addMethod(
                    createGetterBuilder(setOfString, FIELD_UPDATE_PROPERTIES, Modifier.PUBLIC)
                            .addAnnotation(Override.class).build()
            );
        }

        // 声明更新属性
        for (VariableElement variableElement : updateFieldElementList) {
            TypeName typeName = TypeName.get(variableElement.asType());
            String fieldName = variableElement.getSimpleName().toString();
            FieldSpec field = FieldSpec.builder(typeName, fieldName)
                    .addModifiers(Modifier.PRIVATE)
                    .build();
            typeSpecBuilder.addField(field);

            // 同时声明 getter
            typeSpecBuilder.addMethod(createGetter(typeName, fieldName, Modifier.PUBLIC));

            // 内部类声明修改更新属性的方法
            MethodSpec updateMethod = MethodSpec.methodBuilder("with" + toCamelCase(fieldName))
                    .returns(builderClassName)
                    .addModifiers(Modifier.PUBLIC)
                    .addParameter(typeName, fieldName)
                    .addStatement("this.$N.$N = $N", FIELD_UPDATER, fieldName, fieldName)
                    .addStatement("this.$N.$N.add($S)", FIELD_UPDATER, FIELD_UPDATE_PROPERTIES, fieldName)
                    .addStatement("return this")
                    .build();
            buildTypeSpecBuilder.addMethod(updateMethod);
        }

        // 声明构造函数
        {
            MethodSpec constructor = MethodSpec.constructorBuilder()
                    .addModifiers(Modifier.PRIVATE)
                    .addParameter(updateIdTypeName, FIELD_UPDATE_ID)
                    .addStatement("this.$N = $N", FIELD_UPDATE_ID, FIELD_UPDATE_ID)
                    .build();
            typeSpecBuilder.addMethod(constructor);
        }

        // 内部类生成 build 方法
        {
            MethodSpec buildMethod = MethodSpec.methodBuilder(METHOD_BUILD)
                    .addModifiers(Modifier.PUBLIC)
                    .returns(updaterClassName)
                    .addStatement("return this.$N", FIELD_UPDATER)
                    .build();
            buildTypeSpecBuilder.addMethod(buildMethod);
        }

        // 外部类创建 builder 方法
        {
            MethodSpec builderMethod = MethodSpec.methodBuilder(METHOD_BUILDER)
                    .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                    .returns(builderClassName)
                    .addParameter(updateIdTypeName, FIELD_UPDATE_ID)
                    .addStatement("return new $T($N)", builderClassName, FIELD_UPDATE_ID)
                    .build();
            typeSpecBuilder.addMethod(builderMethod);
        }

        TypeSpec builderTypeSpec = buildTypeSpecBuilder.build();

        typeSpecBuilder.addType(builderTypeSpec);

        TypeSpec typeSpec = typeSpecBuilder.build();

        JavaFile javaFile = JavaFile.builder(packageElement.getQualifiedName().toString(), typeSpec).skipJavaLangImports(true).build();

        try {
            javaFile.writeTo(filer);
        } catch (Exception e) {
            System.err.println("QBeanProcessor Write File Error");
            e.printStackTrace();
        }
    }

    private MethodSpec.Builder createGetterBuilder(TypeName typeName, String fieldName, Modifier visibility) {
        String prefix = "get";
        if (typeName.equals(TypeName.get(boolean.class))) {
            prefix = "is";
        }

        return MethodSpec.methodBuilder(prefix + toCamelCase(fieldName))
                .addModifiers(visibility)
                .addStatement("return this.$N", fieldName)
                .returns(typeName);
    }

    private MethodSpec createGetter(TypeName typeName, String fieldName, Modifier visibility) {
        return createGetterBuilder(typeName, fieldName, visibility).build();
    }

    private String toCamelCase(String s) {
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> supportedAnnotationTypes = new LinkedHashSet<>();
        supportedAnnotationTypes.add(QBean.class.getName());
        return supportedAnnotationTypes;
    }

    protected void error(Element e, String msg, Object... args) {
        messager.printMessage(
                Diagnostic.Kind.ERROR,
                String.format(msg, args),
                e);
    }
}
