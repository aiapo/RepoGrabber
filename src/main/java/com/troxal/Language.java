package com.troxal;

public class Language {
    private String name;
    private Integer size;

    public Language(String name,Integer size){
        setName(name);
        setSize(size);
    }

    public void setName(String name){
        this.name=name;
    }
    public String getName(){
        return name;
    }

    public void setSize(Integer size){
        this.size=size;
    }
    public Integer getSize(){
        return size;
    }

    @Override
    public String toString() {
        return "Language "+getName()+" is "+getSize()+" bytes of the repo";
    }
}
