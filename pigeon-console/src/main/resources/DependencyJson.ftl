{"invokers": [
<#list invokers as x>
	{
		"invoker": "${x}"
	}<#if x_has_next>,</#if>
</#list>
],
"clients": [
<#list clients?keys as key>
	{
		"service": "${key}",
		"address":  [
			<#list clients[key] as client>
			{
			"server": "${client}"
			}<#if client_has_next>,</#if>
			</#list>
			]
	}<#if key_has_next>,</#if>
</#list>
],
"serviceAddresses": [
<#list serviceAddresses?keys as key>
	{
		"service": "${key}",
		"addresses":  [
		<#list serviceAddresses[key] as addr>
			{
				"server": "${addr}"
			}<#if addr_has_next>,</#if>
		</#list>
		]
	}<#if key_has_next>,</#if>
</#list>
]
}

