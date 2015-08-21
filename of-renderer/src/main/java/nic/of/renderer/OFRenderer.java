package nic.of.renderer;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by saket on 8/19/15.
 */
public class OFRenderer implements AutoCloseable{
    private static final Logger LOG = LoggerFactory.getLogger(OFRenderer.class);

    private DataBroker dataBroker;

    protected ServiceRegistration nicConsoleRegistration;

    private OFRendererDataChangeListener ofRendererDataChangeListener;

    public OFRenderer(DataBroker dataBroker) {
        this.dataBroker = dataBroker;
    }

    public void init() {
        LOG.info("OF Renderer Provider Session Initiated");

        // Initialize operational and default config data in MD-SAL data store
        BundleContext context = FrameworkUtil.getBundle(this.getClass()).getBundleContext();
        nicConsoleRegistration = context.registerService(OFRenderer.class, this, null);

        ofRendererDataChangeListener = new OFRendererDataChangeListener(dataBroker);
    }
    @Override
    public void close() throws Exception {

    }
}
