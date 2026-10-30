package com.app.helper.network

object ApiConfig {
    // ===== 我们的后端（Go 服务）：账号鉴权 + 指纹下发 =====
    // 手机号即账号：后端做白名单授权，未授权的号拿不到指纹
    const val BACKEND_BASE_URL = "http://103.146.231.72:2238"
    const val BACKEND_LOGIN_PATH = "/api/login"
    const val BACKEND_HEARTBEAT_PATH = "/api/heartbeat"
    const val BACKEND_TASK_PATH = "/api/task"
    const val BACKEND_SUBMIT_PATH = "/api/submit"

    // 指纹环境：全部由后端生成下发（需账号已授权 fp_env_enabled + 当日额度）
    // 客户端不再本地生成任何字段
    const val FP_ENV_GENERATE_PATH = "/api/fp_env/generate"
    val FP_ENV_URL: String get() = BACKEND_BASE_URL + FP_ENV_GENERATE_PATH

    // ===== 任务平台接口（取任务 / 上传任务） =====
    const val TEAM_BASE_URL = BACKEND_BASE_URL
    const val TEAM_TASK_TAKE_PATH = "/api/task"          // GET ?phone=xxx
    const val TEAM_TASK_UPLOAD_PATH = "/task/upload/json" // POST

    // ===== 解密接口 =====
    const val DECRYPT_URL = "http://103.146.231.72:2238/?act=decrypt_test"

    // ===== 旧代理地址（更新检查 / 风控上报等仍走这里） =====
    const val DECRYPT_PROXY_URL = "http://103.146.231.72:2238/decrypt_proxy.php"

    const val TARGET_PACKAGE = "com.shopee.tw"
    const val WAKE_ACTION = "com.oppo.wake.launch.action"

    const val CONTENT_PROVIDER_AUTHORITY = "com.yh.csyxgl.vivo.provider"
    const val CONTENT_PROVIDER_PATH_CURRENT = "current_fp"
    const val CONTENT_PROVIDER_PATH_FINGERPRINT_CURRENT = "fingerprint/current"
}
