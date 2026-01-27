package io.github.bhxch.mcp.jlens.inspector.model;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class InspectorModelTest {

    @Test
    void testParameterInfoBuilder() {
        ParameterInfo param = ParameterInfo.builder()
                .name("testParam")
                .type("java.lang.String")
                .index(0)
                .isVarArgs(false)
                .build();

        assertEquals("testParam", param.getName());
        assertEquals("java.lang.String", param.getType());
        assertEquals(0, param.getIndex());
        assertFalse(param.isVarArgs());
        assertTrue(param.toString().contains("testParam"));
    }

    @Test
    void testFieldInfoBuilder() {
        FieldInfo field = FieldInfo.builder()
                .name("testField")
                .type("int")
                .modifiers(1)
                .isStatic(true)
                .isFinal(false)
                .build();

        assertEquals("testField", field.getName());
        assertEquals("int", field.getType());
        assertTrue(field.isStatic());
        assertFalse(field.isFinal());
        assertTrue(field.toString().contains("testField"));
    }

    @Test
    void testMethodInfoBuilder() {
        ParameterInfo param = ParameterInfo.builder().name("p").type("int").build();
        MethodInfo method = MethodInfo.builder()
                .name("testMethod")
                .returnType("void")
                .modifiers(1)
                .addParameter(param)
                .addException("java.io.IOException")
                .isAbstract(true)
                .build();

        assertEquals("testMethod", method.getName());
        assertEquals("void", method.getReturnType());
        assertEquals(1, method.getParameters().size());
        assertEquals(1, method.getExceptions().size());
        assertTrue(method.isAbstract());
        assertTrue(method.toString().contains("testMethod"));
    }

    @Test
    void testClassMetadataBuilder() {
        FieldInfo field = FieldInfo.builder().name("f").type("int").build();
        MethodInfo method = MethodInfo.builder().name("m").returnType("void").build();
        
        ClassMetadata meta = ClassMetadata.builder()
                .className("com.example.Test")
                .packageName("com.example")
                .simpleClassName("Test")
                .superClass("java.lang.Object")
                .addInterface("java.io.Serializable")
                .addField(field)
                .addMethod(method)
                .isInterface(false)
                .isEnum(false)
                .status("SUCCESS")
                .build();

        assertEquals("com.example.Test", meta.getClassName());
        assertEquals("com.example", meta.getPackageName());
        assertEquals(1, meta.getInterfaces().size());
        assertEquals(1, meta.getFields().size());
        assertEquals(1, meta.getMethods().size());
        assertEquals("SUCCESS", meta.getStatus());
        
        ClassMetadata meta2 = ClassMetadata.builder()
                .className("com.example.Test")
                .build();
        
        assertEquals(meta, meta2);
        assertEquals(meta.hashCode(), meta2.hashCode());
        assertTrue(meta.toString().contains("Test"));
    }
}
