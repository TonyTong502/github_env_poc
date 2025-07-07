<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:e="IPG.EAI.BW.Ariba.TaskMembers"
                exclude-result-prefixes="e">
  <xsl:output indent="yes" method="xml"/>

  <xsl:template match="messages">
    <xsl:apply-templates select="*"/>
  </xsl:template>
  
  <xsl:template match="@*|node()">
    <xsl:param name="text"/>
    <xsl:variable name="LastTagValue" select="(//ExceptionRecords/e.Record/e.IsRetryable)[last()]"/>
    <!--<xsl:text>{
	"results": [</xsl:text>-->
    <root>
    <xsl:for-each select="//message">
      <xsl:if test="ExceptionRecords/e.Record/e.IsRetryable='TRUE'">
        
		<results><ApproverID><xsl:value-of select="ExceptionRecords/e.Record/e.Request"/></ApproverID></results>
        
        <!--<xsl:choose>
          
          <xsl:when test="position() != last()">
              <xsl:text>"},</xsl:text>
          </xsl:when>
        <xsl:otherwise>
          <xsl:text>"}]}</xsl:text>
        </xsl:otherwise>
        </xsl:choose>-->
      </xsl:if>
      <!--<xsl:choose>
        <xsl:when test="position() = last() and $LastTagValue='FALSE'">
            <xsl:text>]}</xsl:text>
        </xsl:when>
      </xsl:choose>-->
    </xsl:for-each>
      </root>
  </xsl:template>
</xsl:stylesheet>