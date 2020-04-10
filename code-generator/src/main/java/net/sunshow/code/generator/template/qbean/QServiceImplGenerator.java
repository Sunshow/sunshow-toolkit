package net.sunshow.code.generator.template.qbean;

import com.squareup.javapoet.*;
import net.sunshow.code.generator.util.GenerateUtils;

import javax.lang.model.element.Modifier;
import java.io.File;
import java.util.Optional;

public class QServiceImplGenerator {

    public static void generate(QTemplate template) throws Exception {
        TypeName abstractServiceImplTypeName = ParameterizedTypeName.get(QTemplate.ClassNameAbstractQServiceImpl, template.getBeanClassName());

        TypeSpec.Builder typeSpecBuilder = TypeSpec.classBuilder(template.getServiceImplName())
                .addModifiers(Modifier.PUBLIC)
                .addSuperinterface(template.getServiceClassName())
                .superclass(abstractServiceImplTypeName)
                .addAnnotation(QTemplate.ClassNameSpringService);

        String repositoryInstance = GenerateUtils.upperCamelToLowerCamel(template.getRepositoryName());
        String entityInstance = GenerateUtils.upperCamelToLowerCamel(template.getEntityName());

        // 添加 repository
        {
            FieldSpec fieldSpec = FieldSpec.builder(template.getRepositoryClassName(), repositoryInstance, Modifier.PRIVATE).build();
            typeSpecBuilder.addField(fieldSpec);
        }

        // 添加默认方法实现
        // 按ID获取
        {
            TypeName optionalTypeName = ParameterizedTypeName.get(ClassName.get(Optional.class), template.getBeanClassName());
            MethodSpec methodSpec = MethodSpec.methodBuilder("getBy" + GenerateUtils.lowerCamelToUpperCamel(template.getIdName()))
                    .addModifiers(Modifier.PUBLIC)
                    .returns(optionalTypeName)
                    .addParameter(template.getIdClassName(), template.getIdName())
                    .addAnnotation(Override.class)
                    .addStatement("return $N.findOne(id).map(this::convertQBean)", repositoryInstance)
                    .build();
            typeSpecBuilder.addMethod(methodSpec);
        }

        // 新建
        {
            MethodSpec methodSpec = MethodSpec.methodBuilder("save")
                    .addModifiers(Modifier.PUBLIC)
                    .returns(template.getBeanClassName())
                    .addParameter(template.getBeanCreatorClassName(), "creator")
                    .addAnnotation(Override.class)
                    .addAnnotation(QTemplate.ClassNameSpringTransactional)
                    .addException(template.getExceptionClassName())
                    .addStatement("$T $N = new $T()", template.getEntityClassName(), entityInstance, template.getEntityClassName())
                    .addCode("\n")
                    .addStatement("$T.copyCreatorField($N, creator)", QTemplate.ClassNameQBeanCreatorHelper, entityInstance)
                    .addCode("\n")
                    .addStatement("return convertQBean($N.save($N))", repositoryInstance, entityInstance)
                    .build();
            typeSpecBuilder.addMethod(methodSpec);
        }

        // 更新
        {
            MethodSpec methodSpec = MethodSpec.methodBuilder("update")
                    .addModifiers(Modifier.PUBLIC)
                    .returns(template.getBeanClassName())
                    .addParameter(template.getBeanUpdaterClassName(), "updater")
                    .addAnnotation(Override.class)
                    .addAnnotation(QTemplate.ClassNameSpringTransactional)
                    .addException(template.getExceptionClassName())
                    .addStatement("$T $N = getEntityWithNullCheckForUpdate(updater.getUpdateId(), $N)", template.getEntityClassName(), entityInstance, repositoryInstance)
                    .addCode("\n")
                    .addStatement("$T.copyUpdaterField($N, updater)", QTemplate.ClassNameQBeanUpdaterHelper, entityInstance)
                    .addCode("\n")
                    .addStatement("return convertQBean($N)", entityInstance)
                    .build();
            typeSpecBuilder.addMethod(methodSpec);
        }

        // 自定义分页查询
        {
            TypeName responseTypeName = ParameterizedTypeName.get(QTemplate.ClassNameQResponse, template.getBeanClassName());
            TypeName pageTypeName = ParameterizedTypeName.get(QTemplate.ClassNameJpaPage, template.getEntityClassName());

            MethodSpec methodSpec = MethodSpec.methodBuilder("findAll")
                    .addModifiers(Modifier.PUBLIC)
                    .returns(responseTypeName)
                    .addParameter(QTemplate.ClassNameQRequest, "request")
                    .addParameter(QTemplate.ClassNameQPage, "requestPage")
                    .addAnnotation(Override.class)
                    .addStatement("return convertQResponse(findAllInternal(request, requestPage))", template.getBeanClassName())
                    .build();
            typeSpecBuilder.addMethod(methodSpec);
        }

        // 自定义分页查询
        {
            TypeName pageTypeName = ParameterizedTypeName.get(QTemplate.ClassNameJpaPage, template.getEntityClassName());
            TypeName specificationTypeName = ParameterizedTypeName.get(QTemplate.ClassNameJpaSpecification, template.getEntityClassName());

            MethodSpec methodSpec = MethodSpec.methodBuilder("findAllInternal")
                    .addModifiers(Modifier.PRIVATE)
                    .returns(pageTypeName)
                    .addParameter(QTemplate.ClassNameQRequest, "request")
                    .addParameter(QTemplate.ClassNameQPage, "requestPage")
                    .addStatement("return $N.findAll(convertSpecification(request), convertPageable(requestPage))", repositoryInstance)
                    .build();
            typeSpecBuilder.addMethod(methodSpec);
        }

        // 异常提供
        {
            WildcardTypeName wildcardExceptionType = WildcardTypeName.subtypeOf(RuntimeException.class);
            TypeName supplierTypeName = ParameterizedTypeName.get(QTemplate.ClassNameSupplier, wildcardExceptionType);

            MethodSpec methodSpec = MethodSpec.methodBuilder("getExceptionSupplier")
                    .addModifiers(Modifier.PROTECTED)
                    .returns(supplierTypeName)
                    .addParameter(String.class, "message")
                    .addParameter(Throwable.class, "cause")
                    .addAnnotation(Override.class)
                    .addStatement("return () -> new $T(message, cause)", template.getExceptionClassName())
                    .build();
            typeSpecBuilder.addMethod(methodSpec);
        }

        // repository setter
        {
            MethodSpec methodSpec = GenerateUtils.createSetterBuilder(template.getRepositoryClassName(), repositoryInstance, Modifier.PUBLIC)
                    .addAnnotation(QTemplate.ClassNameSpringAutowired)
                    .build();
            typeSpecBuilder.addMethod(methodSpec);
        }

        JavaFile javaFile = JavaFile.builder(template.getServiceImplPackagePath(), typeSpecBuilder.build()).indent(template.getIndent()).skipJavaLangImports(true).build();

        javaFile.writeTo(new File(template.getOutputPath()));
    }

}
