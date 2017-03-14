/**
 * &copy; 2017 VCE Company, LLC. All rights reserved.
 * VCE Confidential/Proprietary Information
 */

package com.dell.converged.capabilities.compute.discovered.nodes.client;

import com.dell.converged.capabilities.compute.discovered.nodes.api.ListNodes;
import com.dell.converged.capabilities.compute.discovered.nodes.api.NodesListed;

/**
 * @author Connor Goulding
 */
public interface DiscoveredNodesCapabilityClient
{
    NodesListed execute(ListNodes request);
}
