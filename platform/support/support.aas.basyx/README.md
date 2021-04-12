# AAS Component AAS/BaSxy extension in the Support Layer of the IIP-Ecosphere platform

Asset Administration Shell (AAS) abstraction based on [Eclipse BaSyx](https://www.eclipse.org/basyx/).  

- Currently with implementations for AAS, asset, sub-model, sub-model elements, properties and operations; abilities 
  and other concepts will be added incrementally on demand.
- Load/store abstractions for XML and AASX (for only one AAS)
- Deployment abstractions (local and remote deployment)
- Implementation-level abstractions (direct VAB, not ControlComponent-based)
- AAS Visitor
- shortID validation for recommended/forbidden names. As in BaSyx, shortId must neither be ``value`` nor ``invocationList``. In addition, and more strict than BaSyx, we require that shortIds comply to the RegEx ``[a-zA-Z][a-zA-Z0-9_]+``.

May be, future versions need to switch to lazy loading of deployed AAS instances (supported now by submodel elements collection, but no submodel or AAS).

**Missing**
- Various AAS concepts (incremental addition as needed by the platform)
- Authentication/Security
- Events

**Upgrading BaSyx (for SSE)**

These steps are only needed if you are in charge of upgrading BaSyx and in the same step deploying an new version to the SSE Maven repository. These steps are not intended if you just want to develop for the platform. Then the reliable BaSyx versions in the SSE Maven repository shall be sufficient. 

Why not simply relying on BaSyx in Maven Central? Because, so far BaSyx was not deployed there :/

- Do a local check out of the respective/most recent BaSyx version
- Build each project in ``sdks/java/*`` with ``mvn -DskipTests clean package``.
- Build ``components/basys.components`` with ``mvn -DskipTests clean package``.
- If both are successful, run ``mvn -DskipTests clean install`` on all parts build before.
- Check whether there are now any compile issues in this and try to fix them.
- Run the regression tests of this component.
- Run the regression tests of all dependent components, in particular ``support.iip-aas``, ``transport``, ``connectors`` and ``connectors.aas``.
- If you go for the most recent version, repeat these steps, in particular if significant time was needed to adjust the platform to BaSyx changes.
- Notify all colleagues working on the platform that no commit is permitted until the BaSyx deploment and platform build is completed.
- If successful, do the deployment to the SSE mvn repo via ``jenkins-2`` as user ``jenkins``. You may use the Jenkins tasks, however,
  on command line there is more control over the actual version to be used.
- Checkout BaSxy in ``workspaces/IIP_basyx`` and ``workspaces/IIP_basyx-components``. Verify that the same version from git is used in both folders (so far, separated folders due to two MVN tasks).
    - Run ``mvn -DskipTests clean install`` as before, for the SDK in ``workspaces/IIP_basyx`` and for the components in 
  ``workspaces/IIP_basyx-components``
    - Execute the Jenkins BaSyx deployment task (also to record the time there).
    - The platform will build automatically.
- Notify all colleagues working on the platform that BaSyx was updated, that at least the local Maven shall be forced to update the snapshots and whether an update of the local workspace via git is required.