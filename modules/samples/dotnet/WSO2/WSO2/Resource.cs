using WSO2.Registry;

namespace WSO2
{
    /// <summary>
    /// A representation of a resource on the WSO2 Registry.
    /// </summary>
    /// <remarks>
    /// This class contains logic for managing fetching of content on demand. 
    /// It keeps track of the path (with version) to fetch content on later date.
    /// </remarks>
    public class Resource : WSResource
    {
        protected RegistryClient client;
        protected string pathWithVersion;

        public Resource()
            : base()
        {
        }

        public Resource(RegistryClient client)
            : this()
        {
            this.client = client;
        }

        public Resource(RegistryClient client, WSResource resource)
            : this(client)
        {
            this.authorUserName = resource.authorUserName;
            this.description = resource.description;
            this.mediaType = resource.mediaType;
            this.properties = resource.properties;
            this.createdTime = resource.createdTime;
            this.id = resource.id;
            this.lastModified = resource.lastModified;
            this.lastUpdaterUserName = resource.lastUpdaterUserName;
            this.parentPath = resource.parentPath;
            this.path = resource.path;
            this.matchingSnapshotID = resource.matchingSnapshotID;
        }

        public string PathWithVersion
        {
            set { pathWithVersion = value; }
        }

        public new byte[] contentFile
        {
            get
            {
                if (base.contentFile == null && this.pathWithVersion != null)
                {
                    base.contentFile = client.GetContent(this.pathWithVersion);
                }
                return base.contentFile;
            }
            set { base.contentFile = value; }
        }
    }
}
