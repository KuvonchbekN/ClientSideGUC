package com.wolterskluwer.osatutorial;

import com.wolterskluwer.osa.odata.commons.core.client.request.OperationRequest;
import com.wolterskluwer.osa.referencedomain.odata.api.Operation2;
import com.wolterskluwer.osa.referencedomain.odata.client.ReferencedomainODataClient;
import com.wolterskluwer.osa.referencedomain.odata.client.operationset.Service1OperationSet;
import org.springframework.context.support.GenericXmlApplicationContext;

public class App {
    public static void main(String[] args) {
        GenericXmlApplicationContext refContext = new GenericXmlApplicationContext("context.xml");

        Service1OperationSet service1OperationSet = refContext.getAutowireCapableBeanFactory().getBean(ReferencedomainODataClient.class).getService1OperationSet();

        OperationRequest integer = service1OperationSet.operation2Request(new Operation2());
        String s = integer.toString();
        System.out.println( " =>>>>>> " + s);


    }
}
