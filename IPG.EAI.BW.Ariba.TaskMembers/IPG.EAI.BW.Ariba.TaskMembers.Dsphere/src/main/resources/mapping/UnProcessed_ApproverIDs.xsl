<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:e="IPG.EAI.BW.Ariba.TaskMembers"
                exclude-result-prefixes="e">
  <xsl:output indent="yes" method="xml" omit-xml-declaration="yes"/>
  <xsl:template match="/">
    <Table border="1">
      <th style="color:Red" colspan="4">Unprocessed ApproverIDs:</th>
      <xsl:if test="count(//ApproverID) > 0">
        <xsl:for-each select="//results">
          <tr>
            <td>
              <xsl:value-of select="ApproverID"/>
            </td>
          </tr>
        </xsl:for-each>
      </xsl:if>
    </Table>
    <strong>Note: </strong><xsl:text>Above ApproverIDs will be re-attempted during the next schedule run.</xsl:text>
  </xsl:template>
</xsl:stylesheet>