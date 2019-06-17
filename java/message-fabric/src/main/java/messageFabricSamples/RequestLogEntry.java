/*
 * Copyright 2019 VMware, Inc. All rights reserved. -- VMware Confidential
 */
package messageFabricSamples;

import java.io.Serializable;

public class RequestLogEntry implements Serializable {
   public String request;
   public String response;
   public long requestTimeInMillis;
}
