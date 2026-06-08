package common.extensions;

import com.codeborne.selenide.Configuration;
import common.annotations.Browsers;
import common.annotations.Environments;
import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.util.Arrays;

public class EnvironmentMatchExtension implements ExecutionCondition {
    @Override
    public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext extensionContext) {
        Environments annotation = extensionContext.getElement()
                .map(el -> el.getAnnotation(Environments.class))
                .orElse(null);

        if (annotation == null) {
            return ConditionEvaluationResult.enabled("Нет ограничений к окружению");
        }

        String currentEnvironment = api.configs.Config.getProperty("environment");

        boolean matches = Arrays.stream(annotation.value())
                .anyMatch(env -> env.equals(currentEnvironment));

        if (matches) {
            return ConditionEvaluationResult.enabled("Текущее окружение: " + currentEnvironment);
        }
        {
            return ConditionEvaluationResult.disabled("Тест пропущен, так как текущее окружение " + currentEnvironment +
                    " не находится в списке допустимых окружений для теста: " + Arrays.toString(annotation.value()));
        }
    }
}