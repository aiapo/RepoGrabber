package com.troxal.pojo;

import lombok.Getter;
import java.util.ArrayList;
import java.util.List;

public class MoveAttributeInfo {
    public MoveAttributeInfo(Boolean refactored, String packageName, String className, String classAccess,
                             Boolean classIsAbstract, Boolean classIsStatic, Boolean classIsInner,
                             List<String> outerClasses, Integer classStartLine, Integer classEndLine,
                             Integer classFieldCount, Integer classMethodCount, String fieldName, String fieldAccess,
                             Boolean fieldIsAbstract, Boolean fieldIsStatic, Boolean fieldIsFinal,
                             Integer fieldStartLine, Integer fieldEndLine){
        this.refactored = refactored;
        this.packageName = packageName;
        this.ClassI = new ClassInfo(className, classAccess, classIsAbstract, classIsStatic, classIsInner, outerClasses,
                classStartLine, classEndLine, classFieldCount, classMethodCount);
        this.FieldI = new FieldInfo(fieldName, fieldAccess, fieldIsAbstract, fieldIsStatic, fieldIsFinal,
                fieldStartLine, fieldEndLine);
    }

    public MoveAttributeInfo(){
    }

    @Getter
    private Boolean refactored = false;

    @Getter
    private String packageName = "";

    @Getter
    private ClassInfo ClassI = new ClassInfo();

    @Getter
    private FieldInfo FieldI = new FieldInfo();


    public class ClassInfo {
        public ClassInfo(String name, String access, Boolean isAbstract, Boolean isStatic, Boolean isInnerClass,
                         List<String> outerClasses,
                         Integer startLine, Integer endLine, Integer fieldCount, Integer methodCount){
            this.name = name;
            this.access = access;
            this.isAbstract = isAbstract;
            this.isStatic = isStatic;
            this.isInnerClass = isInnerClass;
            this.outerClasses = outerClasses;
            this.startLine = startLine;
            this.endLine = endLine;
            this.fieldCount = fieldCount;
            this.methodCount = methodCount;
        }

        public ClassInfo(){
        }

        @Getter
        private String name = "";

        @Getter
        private String access = "";

        @Getter
        private List<String> outerClasses = new ArrayList<>();

        @Getter
        private Boolean isAbstract = false;

        @Getter
        private Boolean isStatic = false;

        @Getter
        private Boolean isInnerClass = false;

        @Getter
        private Integer startLine = 0;

        @Getter
        private Integer endLine = 0;

        @Getter
        private Integer fieldCount = 0;

        @Getter
        private Integer methodCount = 0;
    }

    public class FieldInfo {
        public FieldInfo(String name, String access, Boolean isAbstract, Boolean isStatic, Boolean isFinal,
                         Integer startLine, Integer endLine){
            this.name=name;
            this.access=access;
            this.isAbstract=isAbstract;
            this.isStatic=isStatic;
            this.isFinal=isFinal;
            this.startLine=startLine;
            this.endLine=endLine;
        }

        public FieldInfo(){
        }

        @Getter
        private String name = "";

        @Getter
        private String access = "";

        @Getter
        private Boolean isAbstract = false;

        @Getter
        private Boolean isStatic = false;

        @Getter
        private Boolean isFinal = false;

        @Getter
        private Integer startLine = 0;

        @Getter
        private Integer endLine = 0;
    }
}
