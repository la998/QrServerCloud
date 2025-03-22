package com.qr.gateway.config

import com.alibaba.csp.sentinel.adapter.gateway.sc.SentinelGatewayFilter
import com.alibaba.csp.sentinel.adapter.gateway.sc.callback.GatewayCallbackManager
import com.alibaba.csp.sentinel.slots.block.RuleConstant
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager
import jakarta.annotation.PostConstruct
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.Ordered
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.server.ServerWebExchange

@Configuration
class SentinelConfig {

    @PostConstruct
    fun init() {
        initFlowRules()

        GatewayCallbackManager.setBlockHandler { exchange: ServerWebExchange, _: Throwable ->
            ServerResponse.status(HttpStatus.TOO_MANY_REQUESTS)
                .contentType(MediaType.APPLICATION_JSON)
                .body(
                    BodyInserters.fromValue(
                        mapOf(
                            "code" to 429,
                            "message" to "服务繁忙，请稍后重试"
                        )
                    )
                )
        }
    }

    private fun initFlowRules() {
        val rules = listOf(
            FlowRule("auth_route").apply {
                // 仅设置必须属性
                grade = RuleConstant.FLOW_GRADE_QPS  // Kotlin 自动映射到 setGrade()
                count = 100.0                        // Kotlin 自动映射到 setCount()
                controlBehavior = RuleConstant.CONTROL_BEHAVIOR_DEFAULT
            },
            FlowRule("qr_route").apply {
                grade = RuleConstant.FLOW_GRADE_THREAD
                count = 50.0
            }
        )
        FlowRuleManager.loadRules(rules)
    }

    @Bean
    fun sentinelGatewayFilter(): SentinelGatewayFilter {
        // 直接调用构造函数设置 order
        return SentinelGatewayFilter(Ordered.HIGHEST_PRECEDENCE)
    }
}