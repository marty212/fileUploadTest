package API.RecieveData;

import API.Status;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.restlet.data.MediaType;
import org.restlet.ext.fileupload.RestletFileUpload;
import org.restlet.representation.FileRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;

import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by Martin on 2/21/2015.
 */
public class ActionWeb extends ServerResource {

    private static int i = 0;

    @Get
    public Object getter(Representation entity) throws Exception {
        Map map = getQuery().getValuesMap();
        API.Status.StatusBuilder stats = new API.Status.StatusBuilder();
        if (!map.containsKey("number")) {
            stats.append("status", "bad");
            stats.append("extra", "no number included");
            return stats.build().toString();
        }

        int j;
        try {
            j = (int) map.get("number");
        } catch (Exception e) {
            stats.append("status", "bad");
            stats.append("extra", "not an int");
            return stats.build().toString();
        }

        File file = new File(String.valueOf(j));
        if(file.exists())
        {
            return new FileRepresentation(file.getAbsolutePath(), MediaType.MULTIPART_FORM_DATA);
        }
        stats.append("status", "bad");
        stats.append("extra", "file missing");
        return stats.build().toString();
    }
    @Post
    public Object posted(Representation entity) throws Exception
    {
        Representation rep = null;
        API.Status.StatusBuilder stats = new API.Status.StatusBuilder();
        if (entity != null) {
            if (MediaType.MULTIPART_FORM_DATA.equals(entity.getMediaType(),
                    true)) {
                Map<String, String> map = getQuery().getValuesMap();

                // The Apache FileUpload project parses HTTP requests which
                // conform to RFC 1867, "Form-based File Upload in HTML". That
                // is, if an HTTP request is submitted using the POST method,
                // and with a content type of "multipart/form-data", then
                // FileUpload can parse that request, and get all uploaded files
                // as FileItem.

                // 1/ Create a factory for disk-based file items
                DiskFileItemFactory factory = new DiskFileItemFactory();
                factory.setSizeThreshold(1000240);

                // 2/ Create a new file upload handler based on the Restlet
                // FileUpload extension that will parse Restlet requests and
                // generates FileItems.
                RestletFileUpload upload = new RestletFileUpload(factory);
                List<FileItem> items;

                // 3/ Request is parsed by the handler which generates a
                // list of FileItems
                items = upload.parseRequest(getRequest());


                // Process only the uploaded item called "fileToUpload" and
                // save it on disk
                boolean found = false;
                for (final Iterator<FileItem> it = items.iterator(); it
                        .hasNext()
                        && !found;) {
                    FileItem fi = it.next();
                    if (fi.getFieldName().equals("fileToUpload")) {
                        found = true;
                        File file = new File(String.valueOf(i++));
                        fi.write(file);
                    }
                }

                // Once handled, the content of the uploaded file is sent
                // back to the client.
                if (found) {
                    stats.append("status", "good");
                    stats.append("number", String.valueOf(i - 1));
                    return stats.build().toString();
                } else {
                    stats.append("status", "bad");
                    stats.append("extra", "file not found");
                    return stats.build().toString();
                }
            }
        } else {
            stats.append("status", "bad");
            stats.append("extra", "entity missing?");
            return stats.build().toString();
        }
        //should not reach here
        return null;
    }
}
