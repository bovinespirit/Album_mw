<?xml version="1.0" encoding="utf-8"?>
<!-- $Id: sample.xsl,v 1.2 2002/07/31 17:57:57 mfw Exp $ -->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
                xmlns:xalan="http://xml.apache.org/xslt">
 <xsl:output indent="yes" 
             xalan:indent-amount="1" 
             omit-xml-declaration="no" 
             encoding="iso-8859-1" />

 <xsl:template match="albumpage">
  <html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en">

   <head>
    <title>
     <xsl:value-of select="title"/>
    </title>
    <style>
     table.border { border: thin black solid;}
     td.border    { border: thin black solid;}
     th           { text-align: left; }
     td           { text-align: right; }
    </style> 
   </head>

   <body>
    <table border-width="0" width="100%">
     <tr>
      <td width="15em" class="border">
       <xsl:apply-templates select="menu" />
      </td>
      <td> 
       <center>
        <h1>
         <xsl:value-of select="heading"/>
        </h1>
        <h5>
         <xsl:value-of select="description"/>
        </h5>
       </center>

       <center>
        <xsl:copy-of select="text/*" />
       </center> 
        
       <center>
        <table>
         <xsl:call-template name="thumbLayout">
          <xsl:with-param name="n" select="1" />
         </xsl:call-template>
        </table>  
       </center>

       <center>
        <xsl:apply-templates select="image" />
       </center> 

       <center>
        <xsl:apply-templates select="index" />
       </center> 

       <center>
        <table class="border">
         <tr>
          <td class="border">
           <xsl:apply-templates select="link[@type='prevpart']"/>
          </td>	
          <td class="border">
           <xsl:apply-templates select="link[@type='uppart']"/>
          </td>
          <td class="border">
           <xsl:apply-templates select="link[@type='nextpart']"/>
          </td>
         </tr> 	
         <tr>
          <td class="border">
           <xsl:apply-templates select="link[@type='prev']"/>
          </td>
          <td class="border">
           <xsl:apply-templates select="link[@type='index']"/>
          </td>
          <td class="border">
           <xsl:apply-templates select="link[@type='next']"/>
          </td>
         </tr>
        </table>
       </center>
      </td>
     </tr>
    </table>       		 	
   </body>
  </html>
 </xsl:template>

 <xsl:template match="link">
  <a href="{@href}" title="{@description}">
   <xsl:value-of select="."/>
  </a>
 </xsl:template>

 <xsl:template match="image">
  <img src="{@src}" height="{@height}" width="{@width}" />
 </xsl:template> 

 <xsl:template name="thumbLayout">
  <xsl:param name="n" />
  <xsl:if test="thumb[position() = $n]">
   <tr>
    <xsl:call-template name="thumbRow">
     <xsl:with-param name="n" select="$n" />
     <xsl:with-param name="max" select="$n + 3" />
    </xsl:call-template>
   </tr> 
   <xsl:call-template name="thumbLayout">
    <xsl:with-param name="n" select="$n + 3" />
   </xsl:call-template>
  </xsl:if>    
 </xsl:template> 

 <xsl:template name="thumbRow">
  <xsl:param name="n" />
  <xsl:param name="max" />
  <xsl:if test="$n &lt; $max">
   <td>
    <xsl:text> </xsl:text>
    <xsl:apply-templates select="thumb[position() = $n]" />
   </td>
   <xsl:call-template name="thumbRow">
    <xsl:with-param name="n" select="$n+1" />
    <xsl:with-param name="max" select="$max" />
   </xsl:call-template>  
  </xsl:if>   
 </xsl:template> 

 <xsl:template match="thumb">
  <table>
   <tr><th class="border">
    <xsl:value-of select="@title" />
   </th></tr>
   <tr><td class="border">
    <a href="{@href}" title="View large image">
     <xsl:apply-templates select="image" />
    </a>
   </td></tr>
   <tr><td class="border">
    <xsl:value-of select="description" />
   </td></tr>	  
  </table>	
 </xsl:template>

 <xsl:template match="menu">
  <table>
   <xsl:call-template name="menuItem">
    <xsl:with-param name="n" select="1" />
   </xsl:call-template>
  </table> 
 </xsl:template>

 <xsl:template name="menuItem">
  <xsl:param name="n" />
  <xsl:if test="item[position() = $n]">
   <xsl:choose>
    <xsl:when test="item[position() = $n]/@type='section'" >
     <xsl:call-template name="item">
      <xsl:with-param name="item" select="item[position() = $n]" />
      <xsl:with-param name="element" select="'th'" />
     </xsl:call-template>
    </xsl:when>  
    <xsl:when test="item[position() = $n]/@type='page'" >
     <xsl:call-template name="item">
      <xsl:with-param name="item" select="item[position() = $n]" />
      <xsl:with-param name="element" select="'td'" />
     </xsl:call-template>
    </xsl:when>
   </xsl:choose> 
   <xsl:call-template name="menuItem">
    <xsl:with-param name="n" select="$n + 1" />
   </xsl:call-template>
  </xsl:if>
 </xsl:template>   

 <xsl:template name="item">
  <xsl:param name="element" select="'td'" />
  <xsl:param name="item" />
  <tr>
   <xsl:element name="{$element}">
    <xsl:choose>
     <xsl:when test="$item/@href">
      <a class="bovmenu" href="{$item/@href}" title="normalize-space({$item/@description})">
       <xsl:value-of select="normalize-space($item/@title)" />
      </a>
     </xsl:when>
     <xsl:otherwise>
      <xsl:value-of select="$item/@title" />
     </xsl:otherwise>
    </xsl:choose>        
   </xsl:element>
  </tr>  
 </xsl:template>

 <xsl:template match="index">
  <table>
   <xsl:apply-templates select="indeximage" />
  </table>
 </xsl:template>

 <xsl:template match="indeximage">
  <tr>
   <th>
    <a href="{@href}" title="{normalize-space(title)}">
     <xsl:value-of select="normalize-space(title)" />
    </a> 
   </th> 
   <td rowspan="2">
    <a href="{@href}" title="{normalize-space(title)}">
     <xsl:apply-templates select="image" />
    </a> 
   </td>
  </tr>
  <tr> 
   <td>
    <xsl:value-of select="normalize-space(description)" />
   </td>
  </tr>   
 </xsl:template>

</xsl:stylesheet>   
