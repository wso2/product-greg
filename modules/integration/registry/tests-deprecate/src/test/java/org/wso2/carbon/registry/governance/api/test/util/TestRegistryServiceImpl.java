package org.wso2.carbon.registry.governance.api.test.util;

import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.user.core.UserRealm;

public class TestRegistryServiceImpl implements RegistryService {

    Registry registry;

    public TestRegistryServiceImpl(Registry registry){
      this.registry=registry;
    }

    @Override
    public UserRegistry getUserRegistry() throws RegistryException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public UserRegistry getSystemRegistry() throws RegistryException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public UserRegistry getSystemRegistry(int i) throws RegistryException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public UserRegistry getSystemRegistry(int i, String s) throws RegistryException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public UserRegistry getUserRegistry(String s, String s1) throws RegistryException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public UserRegistry getUserRegistry(String s) throws RegistryException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public UserRegistry getUserRegistry(String s, String s1, int i) throws RegistryException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public UserRegistry getUserRegistry(String s, String s1, int i, String s2) throws RegistryException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public UserRegistry getUserRegistry(String s, int i) throws RegistryException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public UserRegistry getUserRegistry(String s, int i, String s1) throws RegistryException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public UserRealm getUserRealm(int i) throws RegistryException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public UserRegistry getRegistry() throws RegistryException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public UserRegistry getRegistry(String s, String s1) throws RegistryException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public UserRegistry getRegistry(String s) throws RegistryException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public UserRegistry getRegistry(String s, String s1, int i) throws RegistryException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public UserRegistry getRegistry(String s, String s1, int i, String s2) throws RegistryException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public UserRegistry getRegistry(String s, int i) throws RegistryException {
        return (UserRegistry)registry;
    }

    @Override
    public UserRegistry getRegistry(String s, int i, String s1) throws RegistryException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public UserRegistry getLocalRepository() throws RegistryException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public UserRegistry getLocalRepository(int i) throws RegistryException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public UserRegistry getConfigSystemRegistry() throws RegistryException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public UserRegistry getConfigSystemRegistry(int i) throws RegistryException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public UserRegistry getConfigUserRegistry() throws RegistryException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public UserRegistry getConfigUserRegistry(String s, String s1) throws RegistryException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public UserRegistry getConfigUserRegistry(String s) throws RegistryException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public UserRegistry getConfigUserRegistry(String s, int i) throws RegistryException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public UserRegistry getConfigUserRegistry(String s, String s1, int i) throws RegistryException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public UserRegistry getGovernanceSystemRegistry() throws RegistryException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public UserRegistry getGovernanceSystemRegistry(int i) throws RegistryException {
        return (UserRegistry)GovernanceUtils.getGovernanceUserRegistry(registry,"admin");
    }

    @Override
    public UserRegistry getGovernanceUserRegistry() throws RegistryException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public UserRegistry getGovernanceUserRegistry(String s, String s1) throws RegistryException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public UserRegistry getGovernanceUserRegistry(String s) throws RegistryException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public UserRegistry getGovernanceUserRegistry(String s, String s1, int i) throws RegistryException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public UserRegistry getGovernanceUserRegistry(String s, int i) throws RegistryException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
