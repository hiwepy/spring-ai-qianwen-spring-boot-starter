package org.springframework.ai.qianwen.autoconfigure;

import com.baidubce.qianfan.Qianfan;
import com.huaweicloud.pangu.dev.sdk.api.llms.LLM;
import com.huaweicloud.pangu.dev.sdk.api.llms.LLMs;
import com.huaweicloud.pangu.dev.sdk.api.llms.config.LLMConfig;
import org.springframework.ai.autoconfigure.mistralai.MistralAiEmbeddingProperties;
import org.springframework.ai.autoconfigure.retry.SpringAiRetryAutoConfiguration;
import org.springframework.ai.model.function.FunctionCallback;
import org.springframework.ai.model.function.FunctionCallbackContext;
import org.springframework.ai.qianwen.QianwenAiChatClient;
import org.springframework.ai.qianwen.QianwenAiEmbeddingClient;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.web.client.RestClientAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * {@link AutoConfiguration Auto-configuration} for 百度千帆 Chat Client.
 */
@AutoConfiguration(after = { RestClientAutoConfiguration.class, SpringAiRetryAutoConfiguration.class })
@EnableConfigurationProperties({ QianwenAiChatProperties.class, QianwenAiConnectionProperties.class, QianwenAiEmbeddingProperties.class })
@ConditionalOnClass(LLMs.class)
public class QianwenAiAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public LLM llm(QianwenAiConnectionProperties properties) {
        //Assert.isNull(properties.getType(), "Qianfan Type must be set");
        Assert.hasText(properties.getAccessKey(), "Qianfan API Access Key must be set");
        Assert.hasText(properties.getSecretKey(), "Qianfan API Secret Key must be set");

        LLMConfig llmConfig = new LLMConfig();
        LLMs.PANGU.setConfig(llmConfig);
        LLMs.of(LLMs.PANGU, llmConfig)
        return new Qianfan(properties.getType().getValue(), properties.getAccessKey(), properties.getSecretKey())
                .setRetryConfig(properties.getRetry())
                .setRateLimitConfig(properties.getRateLimit());
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = QianwenAiChatProperties.CONFIG_PREFIX, name = "enabled", havingValue = "true", matchIfMissing = true)
    public QianwenAiChatClient qianfanAiChatClient(Qianfan qianfan,
                                                   QianwenAiChatProperties chatProperties,
                                                   List<FunctionCallback> toolFunctionCallbacks,
                                                   FunctionCallbackContext functionCallbackContext) {
        if (!CollectionUtils.isEmpty(toolFunctionCallbacks)) {
            chatProperties.getOptions().getFunctionCallbacks().addAll(toolFunctionCallbacks);
        }
        return new QianwenAiChatClient(qianfan, chatProperties.getOptions(), functionCallbackContext);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = MistralAiEmbeddingProperties.CONFIG_PREFIX, name = "enabled", havingValue = "true", matchIfMissing = true)
    public QianwenAiEmbeddingClient qianfanAiEmbeddingClient(Qianfan qianfan, QianwenAiEmbeddingProperties embeddingProperties) {

        return new QianwenAiEmbeddingClient(qianfan, embeddingProperties.getMetadataMode(), embeddingProperties.getOptions());
    }

    @Bean
    @ConditionalOnMissingBean
    public FunctionCallbackContext springAiFunctionManager(ApplicationContext context) {
        FunctionCallbackContext manager = new FunctionCallbackContext();
        manager.setApplicationContext(context);
        return manager;
    }

}
