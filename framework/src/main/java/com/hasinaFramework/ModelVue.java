package com.hasinaFramework;

import java.util.HashMap;
import java.util.Map;

public class ModelVue {
        private String vue;
    private Map<String, Object> data;

    public ModelVue(String vue) {
        this.vue = vue;
        this.data = new HashMap<>();
    }

    public String getPath(){
        return "WEB-INF/views/";
    }

    public String getVue() {
        return this.getPath() + vue;
    }

    public void setVue(String vue) {
        this.vue = vue;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public void setData(Map<String, Object> data) {
        this.data = data;
    }

    public void addData(String key, Object value){
        this.data.put(key, value);
    }
}
