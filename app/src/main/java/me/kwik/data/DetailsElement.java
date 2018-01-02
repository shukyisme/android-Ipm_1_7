package me.kwik.data;

/**
 * Created by Farid Abu Salih on 02/02/17.
 * farid@kwik.me
 */

public class DetailsElement{
    public static final String TYPE_STRING = "String";
    public static final String TYPE_NUMBER = "Number";
    public static final String TYPE_BOOLEAN = "Boolean";

    private String name;
    private String type;
    private int maxLength = Integer.MAX_VALUE;
    private int minLength;
    private String value;
    private String key;
    private boolean isEditable;
    private boolean required = false;

    public boolean isEditable() {
        return isEditable;
    }

    public void setIsEditable(boolean isEditable) {
        this.isEditable = isEditable;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getMaxLength() {
        return maxLength;
    }

    public void setMaxLength(int maxLength) {
        this.maxLength = maxLength;
    }

    public int getMinLength() {
        return minLength;
    }

    public void setMinLength(int minLength) {
        this.minLength = minLength;
    }

    public String isValueValid(){
        String result = null;
        if((value == null || value.trim().length() == 0 ) &&  required){
            return name + (" is required");
        }

        if(value != null && value.trim().length() < minLength){
            return name + " minimum length is " + minLength;
        }

        if(value != null && value.trim().length() > maxLength){
            return  name + " maximum length is " + maxLength;
        }
        return result;
    }

    @Override
    public String toString() {
        return "DetailsElement{" +
                "name='" + name + '\'' +
                ", type=" + type +
                ", maxLength=" + maxLength +
                ", minLength=" + minLength +
                ", value='" + value + '\'' +
                ", key='" + key + '\'' +
                ", isEditable=" + isEditable +
                ", required=" + required +
                '}';
    }
}
