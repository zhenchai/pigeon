{
"paths":
<#if (paths)??>
[
    <#list paths as x>
    {
    "service":"${(x.service)!''}",
    "path":"${(x.path)!''}",
    "group":"${(x.group)!''}",
    "coordinate":{
    "groupId": "${(x.groupId)!''}",
    "artifactId": "${(x.artifactId)!''}",
    "version": "${(x.version)!''}",
    "time": "${(x.time)!''}"
    }
    }
        <#if x_has_next>,</#if>
    </#list>
]
<#else>
[]
</#if>
}
