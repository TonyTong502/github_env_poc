<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:template match="/">
		<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:ns0="http://ipggz.MDM.org/">
			<soapenv:Header/>
			<soapenv:Body>
				<ns0:UploadExtendedLocalMasterData>
					<ns0:localDataList>
						<xsl:for-each select="//row">
							<ns0:ExtendedLocalDataVO>
								<ns0:EntityName>OR</ns0:EntityName>
								<ns0:SourceSystemName>PPS</ns0:SourceSystemName>
								<ns0:HierarchyName>HRH</ns0:HierarchyName>
								<ns0:LocalName>EMP_<xsl:value-of select="EMPLID"/></ns0:LocalName>
								<ns0:LocalDescription>
									<xsl:value-of select="EMAILADDR"/>
								</ns0:LocalDescription>
								<ns0:LocalParentName>OR_PPS_DEP_<xsl:value-of select="SETIDDPT"/>_<xsl:value-of select="DEPTID"/></ns0:LocalParentName>
								<ns0:Status>
									<xsl:value-of select="EMPL_STATUS"/>
								</ns0:Status>
								<ns0:EntityCode>EMP</ns0:EntityCode>
								<ns0:Property1>
									<xsl:value-of select="EMPL_TYPE"/>
								</ns0:Property1>
								<ns0:Property2/>
								<ns0:Property3/>
								<ns0:Property4/>
								<ns0:Property5/>
								<ns0:Property6/>
								<ns0:MappingEntityName/>
								<ns0:MappingHierarchyName/>
								<ns0:MappingParentSourceSystem/>
								<ns0:MappingParentName/>
							</ns0:ExtendedLocalDataVO>
						</xsl:for-each>
					</ns0:localDataList>
				</ns0:UploadExtendedLocalMasterData>
			</soapenv:Body>
		</soapenv:Envelope>
	</xsl:template>
</xsl:stylesheet>