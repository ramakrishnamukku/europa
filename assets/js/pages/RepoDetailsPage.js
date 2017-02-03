/*
  @author Sam Heutmaker [samheutmaker@gmail.com]
*/

import React, {Component, PropTypes} from 'react'
import Loader from './../components/Loader'
import RegistryNames from './../util/RegistryNames'
import RepoSettings from './../components/RepoSettings'
import CenteredConfirm from './../components/CenteredConfirm'
import RegistryProviderIcons from './../util/RegistryProviderIcons'
import DockerPullCommands from './../components/DockerPullCommands'
import NPECheck from './../util/NPECheck'
import RepoEventTimeline from './../components/RepoEventTimeline'
import BtnGroup from './../components/BtnGroup'
import Msg from './../components/Msg'

export default class RepoDetailsPage extends Component {
	constructor(props) {
		super(props);
		this.state = {
			repoId: this.props.params.repoId,
			pollEventsInterval: null
		};
	}
	componentDidMount() {
		this.context.actions.resetRepoDetailsState();
		this.context.actions.listRegistries();
		this.context.actions.toggleRepoDetailsPageXHR(true);
		this.context.actions.listRepos()
		.then(() => {
			let repoDeps = [
				this.context.actions.setActiveRepoDetails(this.state.repoId),
				this.context.actions.listRepoEvents(this.state.repoId),
				this.context.actions.listRepoManifests(this.state.repoId),
				this.context.actions.getRepoOverview(this.state.repoId)
			];
			Promise.all(repoDeps)
			.then(this.context.actions.toggleRepoDetailsPageXHR.bind(this, false))
			.catch(() => {
				this.context.actions.toggleRepoDetailsPageXHR(false);
			})
		})
		
	
		this.setState({
			pollEventsInterval: setInterval(() => {
				this.context.actions.listRepoEvents(this.state.repoId, true);
			}, 10000)
		})
	}
	componentWillUnmount() {
		this.context.actions.resetRepoDetailsState();
		this.context.actions.resetNotifState();
		clearInterval(this.state.pollEventsInterval);
	}
	toRepoList(){
		this.context.router.push('/repositories');
	}
	renderRepoSettings(activeRepo){
		if(this.props.repoDetails.showSettings) {
			return (
				<RepoSettings 
					{...this.props}
					activeRepo={activeRepo}
				/>
			);
		}
	}
	renderDeleteRepo(){
		if(this.props.repoDetails.deleteXHR) {
			return (
				<Loader />
			);
		}

		if(this.props.repoDetails.isDeleting) {
			return (
				<CenteredConfirm message="Are you sure you want to delete this repository? All data will be lost."
							     confirmButtonText="Delete"
							     confirmButtonStyle={{}}
							     onConfirm={() => this.context.actions.deleteActiveRepo(this.toRepoList.bind(this))}
							     onCancel={() => this.context.actions.toggleActiveRepoDelete()}/>
			);
		}
	}
	renderEventTimeline(){
		let events = this.props.repoDetails.events;
		let manifests = this.props.repoDetails.manifests;

		return (
			<RepoEventTimeline 
				{...this.props}
				events={events}
				manifests={manifests}
			/>
		);
	}
	renderPageLoader(){
		return (
			<div className="PageLoader">
				<Loader />
			</div>
		);
	}
	renderError(errorMsg){	
		return (
			<Msg text={errorMsg} style={{padding: '2rem 0'}}/>
		);
	}
	renderHeader(activeRepo){
		return (
			<div className="SmallHeader FlexRow SpaceBetween">
				<div className="FlexColumn Flex1">
					<div className="FlexRow">
						<img src={RegistryProviderIcons(activeRepo.provider)} />
						<h3>{activeRepo.name}</h3>
					</div>
					<span>{RegistryNames(true)[activeRepo.provider]}</span>
				</div>
				<div className="FlexRow">
					{this.renderRepoPullCommands()}
					{this.renderActions()}
				</div>
			</div>
		);
	}
	renderRepoPullCommands(){
		return (
			<DockerPullCommands {...this.props} />
		);
	}
	renderActions(){
		let buttons = [
			{
				icon: 'icon icon-dis-terminate',
			    onClick: () => this.context.actions.toggleActiveRepoDelete(),
				isActive: this.props.repoDetails.isDeleting,
				toolTip: 'Disconnect'
			},
			{
				icon: 'icon icon-dis-settings',
			    onClick: () => this.context.actions.toggleActiveRepoSettings(),
				isActive: this.props.repoDetails.showSettings,
				toolTip: 'Settings'
			}
		];

		return (
			<BtnGroup buttons={buttons} />
		);
	}
	render() {	
		let isBlocked = NPECheck(this.props, 'repoDetails/isBlocked', false);

		if(isBlocked) {
			return (
				<div className="PageBlocked">
					Blocked
				</div>
			);
		}

		let errorMsg = NPECheck(this.props, 'repoDetails/eventsError', false);

		if(errorMsg) {
			return this.renderError(errorMsg);
		}

		if(this.props.repoDetails.pageXHR || this.props.repoDetails.eventsXHR) {
			return this.renderPageLoader()
		}

		let activeRepo = NPECheck(this.props, 'repoDetails/activeRepo', {});

		return (
			<div className="ContentContainer">
				{this.renderHeader(activeRepo)}
				<div>
				 	{this.renderDeleteRepo()}
				    {this.renderRepoSettings(activeRepo)}
				    {this.renderEventTimeline()}
				</div>
			</div>
		);
	}
}

RepoDetailsPage.childContextTypes = {
	actions: PropTypes.object,
    router: PropTypes.object
};

RepoDetailsPage.contextTypes = {
	actions: PropTypes.object,
    router: PropTypes.object
};
