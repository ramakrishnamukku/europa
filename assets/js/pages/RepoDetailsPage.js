import React, {Component} from 'react'
import Loader from './../components/Loader'
import WebhookData from './../components/WebhookData'
import isEmpty from './../util/IsEmpty'
import RegistryNames from './../util/RegistryNames'
import RepoSettings from './../components/RepoSettings'
import CenteredConfirm from './../components/CenteredConfirm'
import RepoEventTimeline from './../components/RepoEventTimeline'

export default class RepoDetailsPage extends Component {
	constructor(props) {
		super(props);
		this.state = {

		};
	}
	componentWillMount() {
		let repoId = this.props.params.repoId

		if(isEmpty(this.context.state.reposMap)) {
			this.context.actions.toggleRepoDetailsPageXHR()
			this.context.actions.listRepos().then(() => {
				this.context.actions.setActiveRepoDetails(repoId);
				this.context.actions.listRepoEvents(repoId);
			});			
		} else {
			this.context.actions.setActiveRepoDetails(repoId);
		}
	}
	componentWillUnmount() {
		this.context.actions.resetRepoDetailsState();
	}
	toRepoList(){
		this.context.router.push('/repositories');
	}
	renderRepoSettings(activeRepo){
		if(this.context.state.repoDetails.showSettings) {
			return (
				<RepoSettings 
					activeRepo={activeRepo}
				/>
			);
		}
	}
	renderDeleteRepo(){

		if(this.context.state.repoDetails.deleteXHR) {
			return (
				<Loader />
			);
		}

		if(this.context.state.repoDetails.isDeleting) {
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
		let fakeEvents = [
			{
				image: 'hyper-local',
				id:'1',
				tag: 'latest',
				eventContent: 'somethign 1'
			},{
				image: 'hyper-local',
				id:'2',
				tag: 'latest',
				eventContent: 'somethign 2'
			},{
				image: 'hyper-local',
				id:'3',
				tag: 'latest',
				eventContent: 'somethign 3'
			}
		];
		return (
			<RepoEventTimeline 
				events={fakeEvents}
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
	render() {	
		if(this.context.state.repoDetails.pageXHR) {
			return this.renderPageLoader()
		}


		let activeRepo = this.context.state.repoDetails.activeRepo;
		return (
			<div className="ContentContainer">
				<div className="SmallPageHeader">
					<h3>
						{activeRepo.name}
					</h3>
					<div className="SubHead">
					 	<span className="PipeSeperator">|</span> {RegistryNames[activeRepo.provider]}
					</div>
					<i className="icon icon-dis-trash" 
					   onClick={() => this.context.actions.toggleActiveRepoDelete()}/>
					<i className="icon icon-dis-settings" 
					   onClick={() => this.context.actions.toggleActiveRepoSettings()}/>
				</div>
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
	actions: React.PropTypes.object,
    state: React.PropTypes.object,
    router: React.PropTypes.object
};

RepoDetailsPage.contextTypes = {
	actions: React.PropTypes.object,
    state: React.PropTypes.object,
    router: React.PropTypes.object
};
