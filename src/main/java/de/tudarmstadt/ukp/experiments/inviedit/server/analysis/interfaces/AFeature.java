/*******************************************************************************
 * Copyright 2016
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package de.tudarmstadt.ukp.experiments.inviedit.server.analysis.interfaces;

import java.util.concurrent.ConcurrentLinkedQueue;

import de.tudarmstadt.ukp.experiments.inviedit.server.analysis.sharedresource.SharedRessources;
import de.tudarmstadt.ukp.experiments.inviedit.shared.feature.Feature;
import de.tudarmstadt.ukp.experiments.inviedit.shared.marker.MarkerTransport;
import de.tudarmstadt.ukp.experiments.inviedit.shared.version.VersionTransport;

/**
 * All Features have to implement this class.
 * {@link AFeature#analyse(VersionTransport)} gets the newest
 * {@link VersionTransport} and is called everytime a new
 * {@link VersionTransport} is send to the server. Here the analysis should take
 * place. <br>
 * {@link AFeature#addNewMarker(MarkerTransport)} should be called from the
 * analysis method to add new Markers in form of {@link MarkerTransport}. These
 * markers will be send back and displayed on the client. <br>
 * {@link AFeature#markVersionAsFinished(int)} should be called to signal that a
 * version is fully processed. This is important for performance reasons since
 * it allows the client to delete older versions.
 */
public abstract class AFeature {

	private ConcurrentLinkedQueue<MarkerTransport> newMarkers = new ConcurrentLinkedQueue<MarkerTransport>();
	private ConcurrentLinkedQueue<Integer> deprecatedVersions = new ConcurrentLinkedQueue<Integer>();
	protected SharedRessources sharedRessources;

	protected AFeature(SharedRessources sharedRessources){
		this.sharedRessources = sharedRessources;
	}

	/**
	 * This method adds a new marker to {@link AFeature#newMarkers}. These new
	 * markers will be send to and displayed on the client
	 *
	 * @param mt
	 *            Transport representation for the new marker
	 */
	protected final void addNewMarker(MarkerTransport mt) {
		this.newMarkers.add(mt);
	}

	/**
	 * This method signals that the implementing feature fully processed the
	 * given version. This is important for the performance of InViEdit, since
	 * it allows the client to no longer cache older versions
	 *
	 * @param versionId
	 *            ID of the fully processed version
	 */
	protected final void markVersionAsFinished(int versionId) {
		this.deprecatedVersions.add(versionId);
	}

	/**
	 * Return the next new marker generated by the implementing feature or null
	 * if there are no more new markers
	 *
	 * @return Next new marker or null if there is none
	 */
	public final MarkerTransport getNextNewMarker() {
		return newMarkers.poll();
	}

	/**
	 * Returns the next fully processed version of the implementing feature or
	 * null if there is none
	 *
	 * @return next fully processed version ID or null if there is none
	 */
	public final Integer getNextDeprecatedVersion() {
		return deprecatedVersions.poll();
	}

	/**
	 * This method should implement the analysis. <br>
	 * ATTENTION: It is called asynchronous multiple times. So make sure it has no side effects, be aware of race conditions
	 * @param vt
	 */
	public abstract void analyse(VersionTransport vt,boolean analyseAll);

	public abstract Feature getFeature();

}