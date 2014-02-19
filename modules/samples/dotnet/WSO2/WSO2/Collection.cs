using WSO2.Registry;

namespace WSO2
{
    /// <summary>
    /// A representation of a collection on the WSO2 Registry.
    /// </summary>
    /// <remarks>
    /// This class extends the <see cref="WSO2.Registry"/> class. This has logic
    /// to manage the list of children of a collection.
    /// </remarks>
    public class Collection : Resource
    {
        private int _childCount = -1;
        private string[] _children;

        public Collection()
            : base()
        {
        }

        public Collection(RegistryClient client)
            : base (client)
        {
        }

        public Collection(RegistryClient client, WSCollection collection)
            : base(client, collection)
        {
            this._childCount = collection.childCount;
        }

        public int childCount
        {
            get
            {
                if (_childCount > 0)
                {
                    return _childCount;
                }
                else if (children != null)
                {
                    _childCount = children.Length;
                    return _childCount;
                }
                return 0;
            }
            set
            {
                _childCount = value;
            }
        }

        public string[] children
        {
            get
            {
                if (_children == null && pathWithVersion != null)
                {
                    _children = base.client.GetCollectionContent(pathWithVersion);
                }
                return (_children != null) ? _children : new string[0];
            }
            set 
            {
                _children = value; 
            }
        }
    }
}
