import React, {Component} from 'react'
import Loader from './../components/Loader'
import WebhookData from './../components/WebhookData'
import isEmpty from './../util/IsEmpty'
import RegistryNames from './../util/RegistryNames'
import RepoSettings from './../components/RepoSettings'

export default class RepoDetailsPage extends Component {
	constructor(props) {
		super(props);
		this.state = {

		};
	}
	componentWillMount() {
		if(isEmpty(this.context.state.reposMap)) {
			this.context.actions.toggleRepoDetailsPageXHR()
			this.context.actions.listRepos().then(() => {
				this.context.actions.setActiveRepoDetails(this.props.params.repo);	
			});			
		} else {
			this.context.actions.setActiveRepoDetails(this.props.params.repo);
		}
	}
	componentWillUnmount() {
		this.context.actions.resetRepoDetailsState();
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
				<div className="RepoPageHeader">
					<div>
						{activeRepo.name}
					</div>

					<div className="SubHead">
					 	{RegistryNames[activeRepo.provider]}
					</div>

					<i className="icon icon-dis-trash" />
					<i className="icon icon-dis-settings" 
					   onClick={() => this.context.actions.toggleActiveRepoSettings()}/>

				</div>
				<div>
				    {this.renderRepoSettings(activeRepo)}
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
