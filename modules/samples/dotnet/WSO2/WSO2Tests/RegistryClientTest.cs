using WSO2;
using Microsoft.VisualStudio.TestTools.UnitTesting;
using System;
using System.Text;

namespace WSO2Tests
{
    
    
    /// <summary>
    ///This is a test class for RegistryClientTest and is intended
    ///to contain all RegistryClientTest Unit Tests
    ///</summary>
    [TestClass()]
    public class RegistryClientTest
    {
        private const string BASE_PATH = "/testDotNet";
        private const string SERVER_URL = "https://localhost:9443/services/";
        private const string USERNAME = "admin";
        private const string PASSWORD = "admin";

        private static RegistryClient client;
        private TestContext testContextInstance;

        public static RegistryClient registry
        {
            get { return client; }
            set { client = value; }
        }

        /// <summary>
        ///Gets or sets the test context which provides
        ///information about and functionality for the current test run.
        ///</summary>
        public TestContext TestContext
        {
            get
            {
                return testContextInstance;
            }
            set
            {
                testContextInstance = value;
            }
        }



        #region Additional test attributes
        // 
        //You can use the following additional attributes as you write your tests:
        //
        //Use ClassInitialize to run code before running the first test in the class
        [ClassInitialize()]
        public static void MyClassInitialize(TestContext testContext)
        {
            registry = new RegistryClient(USERNAME, PASSWORD, SERVER_URL);
            if (registry.ResourceExists(BASE_PATH))
            {
                MyClassCleanup();
            }
        }
        //
        //Use ClassCleanup to run code after all tests in a class have run
        [ClassCleanup()]
        public static void MyClassCleanup()
        {
            registry.Delete(BASE_PATH);
        }
        //
        //Use TestInitialize to run code before running each test
        //[TestInitialize()]
        //public void MyTestInitialize()
        //{
        //}
        //
        //Use TestCleanup to run code after each test has run
        //[TestCleanup()]
        //public void MyTestCleanup()
        //{
        //}
        //
        #endregion


        /// <summary>
        ///A test for RegistryClient Constructor
        ///</summary>
        [TestMethod()]
        public void RegistryClientConstructorTest()
        {
            Assert.IsTrue(registry.ResourceExists("/"));
        }

        /// <summary>
        ///A test for RegistryClient Put operation
        ///</summary>
        [TestMethod()]
        public void RegistryClientPutTest()
        {
            String parentPath = BASE_PATH + "/testPut";
            String resourcePath = parentPath + "/Resource";

            Resource resource = registry.NewResource();
            resource.contentFile = new ASCIIEncoding().GetBytes("some text");
            String output = registry.Put(resourcePath, resource);
            Assert.IsTrue(registry.ResourceExists(resourcePath), "The resource does not exist after the put operation");
            Assert.AreEqual(resourcePath, output, "The returned path is invalid.");

            Collection collection = registry.NewCollection();
            output = registry.Put(resourcePath, collection);
            Assert.IsTrue(registry.ResourceExists(resourcePath), "The collection does not exist after the put operation");
            Assert.AreEqual(resourcePath, output, "The returned path is invalid.");

            registry.Delete(parentPath);
            Assert.IsFalse(registry.ResourceExists(parentPath), "Cleanup of Test Space Failed");
        }

        /// <summary>
        ///A test for RegistryClient Get operation
        ///</summary>
        [TestMethod()]
        public void RegistryClientGetTest()
        {
            String parentPath = BASE_PATH + "/testGet";
            String resourcePath = parentPath + "/Resource";

            Resource resource = registry.NewResource();
            string text = "some text";
            resource.contentFile = new ASCIIEncoding().GetBytes(text);
            string mediatype = "text/plain";
            resource.mediaType = mediatype;
            String output = registry.Put(resourcePath, resource);
            Assert.IsTrue(registry.ResourceExists(resourcePath), "The resource does not exist after the put operation");
            Assert.AreEqual(resourcePath, output, "The returned path is invalid.");

            resource = registry.Get(resourcePath);
            Assert.AreEqual(text, new ASCIIEncoding().GetString(resource.contentFile), "The returned text is not proper.");
            Assert.AreEqual(mediatype, resource.mediaType, "The media type is wrong.");

            resource = registry.Get(parentPath);
            Assert.IsTrue(resource is Collection, "A collection was not returned");
            Collection collection = (Collection)resource;
            Assert.AreEqual(1, collection.childCount, "The child count is wrong");
            Assert.AreEqual(resourcePath, collection.children[0], "The child paths are wrong");

            registry.Delete(parentPath);
            Assert.IsFalse(registry.ResourceExists(parentPath), "Cleanup of Test Space Failed");
        }

        /// <summary>
        ///A test for RegistryClient Delete operation
        ///</summary>
        [TestMethod()]
        public void RegistryClientDeleteTest()
        {
            String parentPath = BASE_PATH + "/testDelete";
            String resourcePath = parentPath + "/Resource";

            Resource resource = registry.NewResource();
            resource.contentFile = new ASCIIEncoding().GetBytes("some text");
            String output = registry.Put(resourcePath, resource);

            registry.Delete(resourcePath);
            Assert.IsFalse(registry.ResourceExists(resourcePath), "Failed to delete the resource");
            resource = registry.Get(parentPath);
            Assert.IsTrue(resource is Collection, "A collection was not returned");
            Collection collection = (Collection)resource;
            Assert.AreEqual(0, collection.childCount, "The child count is wrong");

            registry.Delete(parentPath);
            Assert.IsFalse(registry.ResourceExists(parentPath), "Cleanup of Test Space Failed");
        }

        /// <summary>
        ///A test for RegistryClient Import operation
        ///</summary>
        [TestMethod()]
        public void RegistryClientImportTest()
        {
            String parentPath = BASE_PATH + "/testImport";
            String resourcePath = parentPath + "/Resource";

            Resource resource = registry.NewResource();
            string mediatype = "text/html";
            resource.mediaType = mediatype;
            String output = registry.ImportResource(resourcePath, "http://www.google.com", resource);
            Assert.IsTrue(registry.ResourceExists(resourcePath), "The resource does not exist after the import operation");
            Assert.AreEqual(resourcePath, output, "The returned path is invalid.");

            resource = registry.Get(resourcePath);
            Assert.AreEqual(mediatype, resource.mediaType, "The media type is wrong.");
            Assert.IsTrue(new ASCIIEncoding().GetString(resource.contentFile).Contains("Google Search"), 
                "The returned content is not proper.");

            resource = registry.Get(parentPath);
            Assert.IsTrue(resource is Collection, "A collection was not returned");
            Collection collection = (Collection)resource;
            Assert.AreEqual(1, collection.childCount, "The child count is wrong");
            Assert.AreEqual(resourcePath, collection.children[0], "The child paths are wrong");

            registry.Delete(parentPath);
            Assert.IsFalse(registry.ResourceExists(parentPath), "Cleanup of Test Space Failed");
        }
    }
}
