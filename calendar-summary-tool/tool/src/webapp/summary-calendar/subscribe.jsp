<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai"%>
<% 
	response.setContentType("text/html; charset=UTF-8");
	response.addDateHeader("Expires", System.currentTimeMillis() - (1000L * 60L * 60L * 24L * 365L));
	response.addDateHeader("Last-Modified", System.currentTimeMillis());
	response.addHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0, post-check=0, pre-check=0");
	response.addHeader("Pragma", "no-cache");
%>

<% try{ %>
<jsp:useBean id="msgs" class="org.sakaiproject.util.ResourceLoader" scope="session">
   <jsp:setProperty name="msgs" property="baseName" value="calendar"/>
</jsp:useBean>
<% }catch(Exception e) {return;} %>

<f:view>
<sakai:view title="#{msgs.tool_title}">
	
	<h3><h:outputText value="#{msgs.java_subscribe}"/></h3>
	<sakai:instruction_message value="#{msgs.instructions_subscribe}" />
	
	<h:form id="subscribeForm">
		<%/* BUTTONS */%><p>
		<h:panelGrid styleClass="act" columns="1">
			<h:commandButton
				action="#{SubscribeBean.cancel}"
				value="#{msgs.back}"/>
		</h:panelGrid>       
	</h:form>
</sakai:view>
</f:view>
