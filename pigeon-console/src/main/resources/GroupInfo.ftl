{
    "invoker" : [
        <#list invokerGroupCache?keys as key>
        {
            "service" : "${key}",
            "group" : "${invokerGroupCache[key]}"
        }<#if key_has_next>,</#if>
        </#list>
    ],
    "provider" : [
        <#list providerGroupCache?keys as key>
        {
            "service" : "${key}",
            "group" : "${providerGroupCache[key]}"
        }<#if key_has_next>,</#if>
        </#list>
    ]
}