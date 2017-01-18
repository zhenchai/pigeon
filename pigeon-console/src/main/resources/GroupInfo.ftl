{
    "services" : [
        <#list services?keys as key>
        {
            "service" : "${key}",
            "group" : "${services[key].group}"
        }<#if key_has_next>,</#if>
        </#list>
    ]
}