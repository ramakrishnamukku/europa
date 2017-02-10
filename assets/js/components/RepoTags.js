/*
  @author Sam Heutmaker [samheutmaker@gmail.com]
*/

import React, {Component, PropTypes} from 'react'
import NPECheck from './../util/NPECheck'
import RepoOverview from './RepoOverview'
import RepoEventItem from './../components/RepoEventItem'
import RegistryProviderIcons from './../util/RegistryProviderIcons'
import ConvertTimeFriendly from './../util/ConvertTimeFriendly'
import CleanSha from './../util/CleanSha'
import Loader from './../components/Loader'

export default class RepoTags extends Component {
	constructor(props) {
		super(props);
		this.state = {
			pollEventsInterval: null
		};
	}
	componentDidMount() {
		let repoId = NPECheck(this.props, 'repoDetails/activeRepo/id', '');

		if(!NPECheck(this.props, 'repoDetails/hasRetrievedManifests', true)) {
			 this.context.actions.listRepoManifests(repoId);
		}

		this.setState({
			pollEventsInterval: setInterval(() => {
				this.context.actions.listRepoManifests(repoId, true);
			}, 15000)
		});
	}
	componentWillUnmount() {
		clearInterval(this.state.pollEventsInterval);
	}
	renderRepoEventTags(){
		let activeRepo = NPECheck(this.props, 'repoDetails/activeRepo', {});

		return this.props.manifests.sort((firstTag, secondTag) => (firstTag.pushTime >= secondTag.pushTime) ? -1 : 1 )
								.map((tag, index) => this.renderRepoEventTagItem(tag, index, activeRepo))
	}
	renderRepoEventTagItem(tag, index, activeRepo){
		let time = tag.pushTime;
		let friendlyTime = ConvertTimeFriendly(time);
		let cleanedSha = CleanSha(tag.manifestId)
		let icon = 'icon icon-dis-box-uncheck';

		if(NPECheck(this.props, 'repoDetails/selectedManifests', [])
			.map((manifest) => manifest.manifestId)
			.includes(tag.manifestId)) {
			icon = 'icon icon-dis-box-check';
		}

		return (
			<div key={index} className="RepoTagItem">
				<i className={icon} 
				   data-tip="View Pull Commands For This Tag" 
				   data-for="ToolTipTop" 
				   onClick={() => this.context.actions.toggleSelectedManifest(tag)}/>
				<span className="ImageSha" data-tip={tag.manifestId} data-for="ToolTipTop">	
					{cleanedSha}
				</span>
				<span className="Tags">
					{tag.tags.map((tag, index) => {
						return (
							<span className="Tag" key={index}>{tag}</span>
						);
					})}
				</span>
				<span className="Size">
					<span className="Label">Virtual Size:&nbsp;</span>
					<span className="Value">{(tag.virtualSize) ? `${Math.ceil(tag.virtualSize/1000000)}M` : 'Unknown'}</span>
				</span>
				<span className="Pushed">
					<span className="Label">Pushed:&nbsp;</span>
					<span className="Value">{friendlyTime}</span>
				</span>
			</div>
		);
	}
	renderNoContent(overrideContent){
		let content = [
			<h3 key={1}>
				No Tags Found
			</h3>,
			<p key={2}> If you just added this repository, it may take a second to populate historical data for this repository.</p>
		];

		if(overrideContent) content = overrideContent;

		return (
			<div className="TimelineContainer">
				<div className="Timeline">
					<div className="NoContent">
						{content}				
					</div>
				</div>
			</div>
		);
	}
	render() {
		let content = this.renderRepoEventTags();

		if(!NPECheck(this.props, 'manifests/length', true)) {
			content = this.renderNoContent();
		}

		if(NPECheck(this.props, 'repoDetails/manifestsXHR', false)) {
			content = this.renderNoContent(<Loader />);
		}

		return (
			<div className="TagsContainer">
				{content}
			</div>
		);
	}	
}

RepoTags.propTypes = {
	manifests: PropTypes.array,
};

RepoTags.childContextTypes = {
    actions: PropTypes.object
};

RepoTags.contextTypes = {
    actions: PropTypes.object
};

