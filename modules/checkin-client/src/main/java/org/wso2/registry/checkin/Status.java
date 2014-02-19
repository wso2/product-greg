package org.wso2.registry.checkin;

import org.apache.axiom.om.OMElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.registry.synchronization.SynchronizationConstants;
import org.wso2.carbon.registry.synchronization.SynchronizationException;
import org.wso2.carbon.registry.synchronization.Utils;

import javax.xml.namespace.QName;
import java.io.File;
import java.io.FilenameFilter;
import java.util.LinkedList;
import java.util.List;

public class Status {
    private ClientOptions clientOptions;
    private List<String> added = new LinkedList<String>();
    private List<String> deleted = new LinkedList<String>();
    private List<String> updated = new LinkedList<String>();
    private Log log = LogFactory.getLog(Status.class);

    public Status(ClientOptions clientOptions) {
        this.clientOptions = clientOptions;
    }

    public void execute() throws SynchronizationException {
        String path;
        if(clientOptions.getTargetResource() != null) {
            path = clientOptions.getTargetResource();
        } else {
            path = clientOptions.getWorkingLocation();
        }
        printStatusRecursively(path);
    }
    private void printStatusRecursively(String path) throws SynchronizationException {
        File file = new File(path);
        OMElement metaElement = Utils.getMetaOMElement(path);

        if (metaElement == null) {
            System.out.println("? "+path);
            return;
        }

        String status = metaElement.getAttributeValue(new QName("status"));
        if(status != null) {
            if(status.equals("added")){
                added.add(path);
                System.out.println("A "+path);
            } else if(status.equals("updated")){
                updated.add(path);
                System.out.println("U "+path);
            } else if(status.equals("deleted")){
                deleted.add(path);
                System.out.println("D "+path);
            }
        } else if (!file.isDirectory()) {
            String metaMd5 = metaElement.getAttributeValue(new QName("md5"));
            if(!Utils.getMD5(file).equals(metaMd5)) {
                updated.add(path);
                System.out.println("U "+path);
            }
        }

        if(file.isDirectory()){
            for(String fileName : file.list(new FilenameFilter() {
                public boolean accept(File file, String s) {
                    if(SynchronizationConstants.META_DIRECTORY.equals(s)){
                        return false;
                    }
                    return true;
                }
            })){
                printStatusRecursively(path + File.separator + fileName);
            }
        }
    }

    public List<String> getAdded() {
        return added;
    }

    public List<String> getDeleted() {
        return deleted;
    }

    public List<String> getUpdated() {
        return updated;
    }
}
