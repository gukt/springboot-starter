package com.example.common;

import org.springframework.http.converter.json.MappingJacksonValue;

import java.util.HashMap;
import java.util.Map;

/**
 * JsonViews class
 */
public class JsonViews {

    private static final Map<String, Class<? extends ApiResultView>> viewClassesByName = new HashMap<>();

    static {
        for (Class<?> clazz : JsonViews.class.getClasses()) {
            if (ApiResultView.class.isAssignableFrom(clazz)) {
                viewClassesByName.put(clazz.getSimpleName(), (Class<ApiResultView>) clazz);
                JsonViewAlias annotation = clazz.getAnnotation(JsonViewAlias.class);
                if (annotation != null) {
                    for (String alias : annotation.value()) {
                        viewClassesByName.put(alias, (Class<ApiResultView>) clazz);
                    }
                }
            }
        }
    }

    // JsonView interfaces

    public static MappingJacksonValue wrap(Object data, String viewName) {
        if (!(data instanceof ApiResponse<?>)) {
            data = ApiResponse.success(data);
        }
        MappingJacksonValue bodyContainer = new MappingJacksonValue(data);
        Class<?> viewClass = getJsonViewClass(viewName);
        if (viewClass != null) {
            bodyContainer.setSerializationView(viewClass);
        }
        return bodyContainer;
    }

    @SuppressWarnings("unchecked")
    private static Class<? extends ApiResultView> getJsonViewClass(String viewName) {
        if (viewName == null) {
            return null;
        }
        Class<? extends ApiResultView> viewClass = viewClassesByName.get(viewName);
        if (viewClass == null) {
            String packageName = JsonViews.class.getName();
            String className = packageName + "$" + viewName;
            try {
                viewClass = (Class<ApiResultView>) Class.forName(className);
                JsonViewAlias alias = viewClass.getAnnotation(JsonViewAlias.class);
                if (alias != null) {
                    for (String v : alias.value()) {
                        viewClassesByName.put(v, viewClass);
                    }
                }
                viewClassesByName.put(viewName, viewClass);
            } catch (ClassNotFoundException e) {
                throw new UnsupportedJsonViewException(viewName);
            }
        }
        return viewClass;
    }

    interface ApiResultView {
    }

    @JsonViewAlias({"id-only"})
    public interface IdOnly extends ApiResultView {
    }

    @JsonViewAlias({"with-attr"})
    public interface WithAttrs extends IdOnly {
    }

    @JsonViewAlias({"user-simple"})
    public interface UserSimpleView extends IdOnly {
    }

    @JsonViewAlias({"user-detail"})
    public interface UserDetailView extends UserSimpleView {
    }

    public static class UnsupportedJsonViewException extends RuntimeException {
        UnsupportedJsonViewException(String view) {
            super("Unsupported view: " + view);
        }
    }
}
