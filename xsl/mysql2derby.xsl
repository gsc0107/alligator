<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet

	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	version="1.1">
<xsl:output method="text"/>

<xsl:template match="/">
<xsl:apply-templates/>
</xsl:template>

 
<xsl:template match="mysqldump">
<xsl:apply-templates/>
</xsl:template>

<xsl:template match="database">

drop schema <xsl:value-of select="@name"/> RESTRICT;
create schema <xsl:value-of select="@name"/>;
set schema <xsl:value-of select="@name"/>;
<xsl:apply-templates select="table_structure"/>
</xsl:template>

<xsl:template match="table_structure">

<xsl:apply-templates select="key[@Seq_in_index='1']" mode="dropindex"/>

drop table <xsl:value-of select="@name"/>;

create table <xsl:value-of select="@name"/>
	(
	id INT not null primary key,
	<xsl:for-each select="field">
	<xsl:if test="position()&gt;1">,</xsl:if>
	<xsl:apply-templates select="."/>
	</xsl:for-each>
	);
<xsl:apply-templates select="key[@Seq_in_index='1']" mode="createindex"/>
</xsl:template>
 
<xsl:template match="field"> 

<xsl:value-of select="@Field"/>
<xsl:text> </xsl:text>
<xsl:choose>
  <xsl:when test="starts-with(@Type,'int(')">int</xsl:when>
  <xsl:when test="starts-with(@Type,'smallint(')">smallint</xsl:when>
  <xsl:when test="starts-with(@Type,'varchar(')"><xsl:value-of select="@Type"/></xsl:when>
 <xsl:otherwise>clob</xsl:otherwise>
</xsl:choose>
<xsl:text> </xsl:text>
<xsl:choose>
<xsl:when test="@Null='NO'">NOT NULL</xsl:when>
<xsl:otherwise>NULL</xsl:otherwise>
</xsl:choose>
</xsl:template> 

<xsl:template match="key" mode="createindex">
<xsl:variable name="keyname" select="@Key_name"/>
create <xsl:if test="@Non_unique!='1'">UNIQUE</xsl:if> index <xsl:value-of select="concat('key_',../@name,'_',$keyname)"/> ON <xsl:value-of select="@Table"/>(<xsl:for-each select="../key[@Key_name=$keyname]"><xsl:if test="position()&gt;1">,</xsl:if><xsl:value-of select="@Column_name"/></xsl:for-each>);
</xsl:template>
 
<xsl:template match="key" mode="dropindex">
<xsl:variable name="keyname" select="@Key_name"/>
drop index <xsl:value-of select="concat('key_',../@name,'_',@Key_name)"/> ;
</xsl:template> 
 
</xsl:stylesheet>
