<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"  xmlns:e="IPG_EAI_BW_Ariba_VendorWorkspacesAndDocuments" exclude-result-prefixes="e">
  <xsl:output indent="yes" method="xml" omit-xml-declaration="yes"/>
  <xsl:template match="messages">
		<xsl:apply-templates select="*"/>
	</xsl:template>
	<xsl:template match="@*|node()">
	    <xsl:choose>
	        <xsl:when test="count(//message) > 0">
                <Table border="1" style="width:100%; table-layout:fixed; word-wrap:break-word; border-collapse:collapse;">
                    <tr>
                      <th style="color:Red; text-align:left; padding:8px;">Unprocessed Records:</th>
                    </tr>
                    <xsl:for-each select="//e.Request[not(. = preceding::e.Request)]">
                      <tr>
                        <td style="padding:8px;">
                          <xsl:value-of select="."/>
                        </td>
                      </tr>
                    </xsl:for-each>
                </Table>
            </xsl:when>
            <xsl:otherwise>
                <strong><li style="color:green">Note: All Records Processed Successfully.</li></strong>
            </xsl:otherwise>
        </xsl:choose>
	</xsl:template>
</xsl:stylesheet>