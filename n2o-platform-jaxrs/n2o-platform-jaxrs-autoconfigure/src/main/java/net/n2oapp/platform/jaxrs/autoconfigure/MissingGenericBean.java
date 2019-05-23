package net.n2oapp.platform.jaxrs.autoconfigure;

import net.n2oapp.platform.jaxrs.TypedParamConverter;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.autoconfigure.condition.*;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.ResolvableType;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.core.type.MethodMetadata;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import java.lang.reflect.Type;
import java.util.Arrays;

public class MissingGenericBean extends SpringBootCondition {

    @Override
    public ConditionOutcome getMatchOutcome(ConditionContext context, AnnotatedTypeMetadata metadata) {

        if (metadata instanceof MethodMetadata) {
            ConditionMessage matchMessage = ConditionMessage.empty();
            matchMessage.andCondition(MissingGenericBean.class.getSimpleName())
                    .didNotFind("any beans").atAll();
            ConfigurableListableBeanFactory beanFactory = context.getBeanFactory();
            String[] beanNamesForType = beanFactory.getBeanNamesForType(TypedParamConverter.class);
            if (beanNamesForType.length == 0) {

                return new ConditionOutcome(true, matchMessage);
            }

            try {
                Type genericReturnType = Class.forName(((MethodMetadata) metadata).getDeclaringClassName()).getMethod(((MethodMetadata) metadata).getMethodName()).getGenericReturnType();
                beanNamesForType = beanFactory.getBeanNamesForType(ResolvableType.forType(genericReturnType));
                if (beanNamesForType.length == 0) {
                    return new ConditionOutcome(true, matchMessage);
                } else {
                    StringBuilder reason = new StringBuilder();
                    reason.append("found beans named ");
                    reason.append(StringUtils
                            .collectionToDelimitedString(Arrays.asList(beanNamesForType), ", "));
                    return ConditionOutcome.noMatch(ConditionMessage
                            .forCondition(MissingGenericBean.class.getSimpleName())
                            .because(reason.toString()));
                }
            } catch (NoSuchMethodException | ClassNotFoundException e) {
                throw new IllegalStateException(e);
            }
        }
        throw new IllegalArgumentException("may be annotated only method");
    }
}
