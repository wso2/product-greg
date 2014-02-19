using System;
using System.Net;
using System.Net.Security;
using System.Security;
using System.Security.Cryptography.X509Certificates;
using System.ServiceModel;
using System.ServiceModel.Channels;
using System.Web.Services;
using WSO2.Authentication;
using WSO2.Registry;
using System.Text;
using System.Xml;
using System.ServiceModel.Security;

namespace WSO2
{
    /// <summary>
    /// A client for remotely accessing the WSO2 Registry
    /// </summary>
    public class RegistryClient
    {
        private String cookie;
        private WSRegistryServicePortTypeClient client;
        private delegate void Del();

        #region Registry initialization and Cookie Management logic

        /// <summary>
        /// Constructor accepting server URL and the credentials used to connect to the server.
        /// </summary>
        /// <param name="username">a valid username of a user that has permissions to use
        /// the WSO2 Registry WS-API.</param>
        /// <param name="password">the password of the user</param>
        /// <param name="serverURL">the URL of the server</param>
        public RegistryClient(String username, String password, String serverURL)
            : this(username, password, serverURL, CreateDefaultBinding())
        {
        }

        /// <summary>
        /// Constructor accepting server URL and the credentials used to connect to the server.
        /// </summary>
        /// <param name="username">a valid username of a user that has permissions to use
        /// the WSO2 Registry WS-API.</param>
        /// <param name="password">the password of the user</param>
        /// <param name="serverURL">the URL of the server</param>
        /// <param name="binding">the binding configuration to be used</param>
        public RegistryClient(String username, String password, String serverURL, Binding binding)
        {
            cookie = null;
            client = GetClient(username, password, serverURL, binding);
        }

        [WebMethod(EnableSession = true)]
        private WSRegistryServicePortTypeClient GetClient(String username, String password, 
            String serverURL, Binding binding)
        {
            ServicePointManager.ServerCertificateValidationCallback = 
                new RemoteCertificateValidationCallback(
                    delegate(object sender, X509Certificate certificate, 
                    X509Chain chain, SslPolicyErrors policyErrors) { return true; });
            AuthenticationAdminPortTypeClient authenticationAdmin =
                new AuthenticationAdminPortTypeClient(binding, 
                    new EndpointAddress(serverURL + "AuthenticationAdmin"));
            using (new OperationContextScope(authenticationAdmin.InnerChannel))
            {
                if (!authenticationAdmin.login(username, password, "127.0.0.1"))
                {
                    throw new SecurityException("Failed to login to system.");
                }
                HttpResponseMessageProperty response = (HttpResponseMessageProperty) 
                    System.ServiceModel.OperationContext.Current.
                    IncomingMessageProperties[HttpResponseMessageProperty.Name];
                cookie = response.Headers[HttpResponseHeader.SetCookie];
            }

            return new WSRegistryServicePortTypeClient(binding, 
                    new EndpointAddress(serverURL + "WSRegistryService"));
        }

        private static BasicHttpBinding CreateDefaultBinding()
        {
            BasicHttpBinding binding = new BasicHttpBinding();
            binding.CloseTimeout = new TimeSpan(0, 1, 0);
            binding.OpenTimeout = new TimeSpan(0, 1, 0);
            binding.SendTimeout = new TimeSpan(0, 10, 0);
            binding.ReceiveTimeout = new TimeSpan(0, 10, 0);
            binding.AllowCookies = false;
            binding.BypassProxyOnLocal = false;
            binding.HostNameComparisonMode = HostNameComparisonMode.StrongWildcard;
            binding.MaxBufferSize = 65536;
            binding.MaxReceivedMessageSize = 65536L;
            binding.MaxBufferPoolSize = 524288L;
            binding.MessageEncoding = WSMessageEncoding.Text;
            binding.TextEncoding = Encoding.UTF8;
            binding.TransferMode = TransferMode.Buffered;
            binding.UseDefaultWebProxy = true;
            XmlDictionaryReaderQuotas readerQuotas = new XmlDictionaryReaderQuotas();
            readerQuotas.MaxDepth = 32;
            readerQuotas.MaxStringContentLength = 8192;
            readerQuotas.MaxArrayLength = 16384;
            readerQuotas.MaxBytesPerRead = 4096;
            readerQuotas.MaxNameTableCharCount = 16384;
            binding.ReaderQuotas = readerQuotas;
            BasicHttpSecurity security = new BasicHttpSecurity();
            security.Mode = BasicHttpSecurityMode.Transport;
            HttpTransportSecurity transport = new HttpTransportSecurity();
            transport.ClientCredentialType = HttpClientCredentialType.None;
            transport.ProxyCredentialType = HttpProxyCredentialType.None;
            transport.Realm = string.Empty;
            security.Transport = transport;
            BasicHttpMessageSecurity message = new BasicHttpMessageSecurity();
            message.ClientCredentialType = BasicHttpMessageCredentialType.UserName;
            message.AlgorithmSuite = SecurityAlgorithmSuite.Default;
            security.Message = message;
            binding.Security = security;
            return binding;
        }

        [WebMethod(EnableSession = true)]
        private void execute(Del operation)
        {
            using (new OperationContextScope(client.InnerChannel))
            {
                if (cookie != null)
                {
                    HttpRequestMessageProperty request = new HttpRequestMessageProperty();
                    request.Headers[HttpRequestHeader.Cookie] = cookie;
                    System.ServiceModel.OperationContext.Current.
                        OutgoingMessageProperties[HttpRequestMessageProperty.Name] = request;
                }
                operation();
            }
    
        }

        #endregion

        #region Utility Operations

        /// <summary>
        /// Fetches content from a resource.
        /// </summary>
        /// <param name="path">The resource path.</param>
        /// <returns>content as a byte array.</returns>
        public byte[] GetContent(String path)
        {
            byte[] output = null;
            execute(delegate() { output = client.getContent(path); });
            return output;
        }

        /// <summary>
        /// Fetches content from a collection.
        /// </summary>
        /// <param name="path">The collection path.</param>
        /// <returns>list of children as a string array.</returns>
        public string[] GetCollectionContent(String path)
        {
            string[] output = null;
            execute(delegate() { output = client.getCollectionContent(path); });
            return output;
        }

        #endregion

        #region Implementation of Core Registry Operations

        /// <summary>
        /// Creates a new resource.
        /// </summary>
        /// <returns>the created resource.</returns>
        public Resource NewResource()
        {
            return new Resource(this);
        }

        /// <summary>
        /// Creates a new collection.
        /// </summary>
        /// <returns>the created collection.</returns>
        public Collection NewCollection()
        {
            return new Collection(this);
        }

        /// <summary>
        /// Check whether a resource exists at the given path
        /// </summary>
        /// <param name="path">Path of the resource to be checked</param>
        /// <returns>true if a resource exists at the given path, false otherwise.</returns>
        public bool ResourceExists(string path)
        {
            bool output = false;
            execute(delegate() { output = client.resourceExists(path); });
            return output;
        }

        /// <summary>
        /// Adds or updates resources in the registry. If there is no resource at the 
        /// given path, resource is added. If a resource already exist at the given path,
        /// it will be replaced with the new resource.
        /// </summary>
        /// <param name="path">the path which we'd like to use for the new resource.</param>
        /// <param name="resource">Resource instance for the new resource</param>
        /// <returns>the actual path that the server chose to use for our Resource</returns>
        public String Put(String path, Resource resource)
        {
            String output = null;
            execute(delegate() { output = client.WSput(path, ResourceToWSResource(resource)); });
            return output;
        }

        private static WSResource ResourceToWSResource(Resource resource)
        {
            WSResource toAdd = (resource is Collection) ? new WSCollection() : new WSResource();
            toAdd.authorUserName = resource.authorUserName;
            toAdd.description = resource.description;
            toAdd.mediaType = resource.mediaType;
            toAdd.properties = resource.properties;
            toAdd.createdTime = resource.createdTime;
            toAdd.id = resource.id;
            toAdd.lastModified = resource.lastModified;
            toAdd.lastUpdaterUserName = resource.lastUpdaterUserName;
            toAdd.parentPath = resource.parentPath;
            toAdd.path = resource.path;
            toAdd.matchingSnapshotID = resource.matchingSnapshotID;
            toAdd.contentFile = resource.contentFile;

            if (resource is Collection)
            {
                toAdd.collection = true;
                ((WSCollection)toAdd).children = ((Collection)resource).children;
                ((WSCollection)toAdd).childCount = ((Collection)resource).childCount;
            }
            return toAdd;
        }

        /// <summary>
        /// Returns the resource at the given path.
        /// </summary>
        /// <param name="path">Path of the resource. e.g. /project1/server/deployment.xml</param>
        /// <returns>Resource instance</returns>
        public Resource Get(String path)
        {
            WSResource resource = null;
            execute(delegate() { resource = client.WSget(path); });
            Resource output = resource.collection ? 
                new Collection(this, (WSCollection)resource) 
                : new Resource(this, resource);
            output.PathWithVersion = path;
            return output;
        }

        /// <summary>
        /// Returns the Collection at the given path, with the content paginated 
        /// according to the arguments.
        /// </summary>
        /// <param name="path">the path of the collection. MUST point to a collection!</param>
        /// <param name="start">the initial index of the child to return. If 
        /// there are fewer children than the specified value, a RegistryException will 
        /// be thrown.</param>
        /// <param name="pageSize">the maximum number of results to return</param>
        /// <returns>a Collection containing the specified results in the content</returns>
        public Collection Get(String path, int start, int pageSize)
        {
            WSCollection collection = null;
            execute(delegate() { collection = 
                client.WSgetWithPageSize(path, start, pageSize); });
            Collection output = new Collection(this, collection);
            output.PathWithVersion = path;
            return output;
        }

        /// <summary>
        /// Deletes the resource at the given path. If the path refers to a directory, 
        /// all child resources of the directory will also be deleted.
        /// </summary>
        /// <param name="path">Path of the resource to be deleted.</param>
        public void Delete(String path)
        {
            execute(delegate() { client.delete(path); });
        }

        /// <summary>
        /// Returns the meta data of the resource at a given path.
        /// </summary>
        /// <param name="path">Path of the resource. e.g. /project1/server/deployment.xml</param>
        /// <returns>Resource instance</returns>
        public Resource GetMetaData(String path)
        {
            WSResource resource = null;
            execute(delegate() { resource = client.WSgetMetaData(path); });
            Resource output = new Resource(this, resource);
            output.PathWithVersion = path;
            return output;
        }

        /// <summary>
        /// Creates a resource by fetching the resource content from the given URL.
        /// </summary>
        /// <param name="path">path where we'd like to add the new resource. 
        /// Although this path is specified by the caller of the method, resource 
        /// may not be actually added at this path.</param>
        /// <param name="url">where to fetch the resource content</param>
        /// <param name="resource">a template resource</param>
        /// <returns>actual path to the new resource</returns>
        public String ImportResource(String path, String url, Resource resource)
        {
            String output = null;
            execute(delegate() { output = client.WSimportResource(path, url, 
                ResourceToWSResource(resource)); });
            return output;
        }

        #endregion

    }
}
