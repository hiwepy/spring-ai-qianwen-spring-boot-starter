package org.springframework.ai.aliyun.dashscope.autoconfigure;

import com.alibaba.dashscope.aigc.generation.Generation;
import com.baidubce.qianfan.Qianfan;
import com.huaweicloud.pangu.dev.sdk.api.llms.LLM;
import com.huaweicloud.pangu.dev.sdk.api.llms.LLMs;
import com.huaweicloud.pangu.dev.sdk.api.llms.config.LLMConfig;
import org.springframework.ai.aliyun.dashscope.AliyunAiDashscopeChatClient;
import org.springframework.ai.aliyun.dashscope.AliyunAiDashscopeEmbeddingClient;
import org.springframework.ai.autoconfigure.mistralai.MistralAiEmbeddingProperties;
import org.springframework.ai.autoconfigure.retry.SpringAiRetryAutoConfiguration;
import org.springframework.ai.model.function.FunctionCallback;
import org.springframework.ai.model.function.FunctionCallbackContext;
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
@EnableConfigurationProperties({ AliyunAiDashscopeChatProperties.class, AliyunAiDashscopeConnectionProperties.class, AliyunAiDashscopeEmbeddingProperties.class })
@ConditionalOnClass(Generation.class)
public class AliyunAiDashscopeAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public LLM llm(AliyunAiDashscopeConnectionProperties properties) {
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
    @ConditionalOnProperty(prefix = AliyunAiDashscopeChatProperties.CONFIG_PREFIX, name = "enabled", havingValue = "true", matchIfMissing = true)
    public AliyunAiDashscopeChatClient qianfanAiChatClient(Qianfan qianfan,
                                                           AliyunAiDashscopeChatProperties chatProperties,
                                                           List<FunctionCallback> toolFunctionCallbacks,
                                                           FunctionCallbackContext functionCallbackContext) {
        if (!CollectionUtils.isEmpty(toolFunctionCallbacks)) {
            chatProperties.getOptions().getFunctionCallbacks().addAll(toolFunctionCallbacks);
        }
        return new AliyunAiDashscopeChatClient(qianfan, chatProperties.getOptions(), functionCallbackContext);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = MistralAiEmbeddingProperties.CONFIG_PREFIX, name = "enabled", havingValue = "true", matchIfMissing = true)
    public AliyunAiDashscopeEmbeddingClient qianfanAiEmbeddingClient(Qianfan qianfan, AliyunAiDashscopeEmbeddingProperties embeddingProperties) {

        return new AliyunAiDashscopeEmbeddingClient(qianfan, embeddingProperties.getMetadataMode(), embeddingProperties.getOptions());
    }

    @Bean
    @ConditionalOnMissingBean
    public FunctionCallbackContext springAiFunctionManager(ApplicationContext context) {
        FunctionCallbackContext manager = new FunctionCallbackContext();
        manager.setApplicationContext(context);
        return manager;
    }

}
