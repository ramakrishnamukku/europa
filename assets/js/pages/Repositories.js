/*
  @author Sam Heutmaker [samheutmaker@gmail.com]
*/

import React, {Component, PropTypes} from 'react'
import { Link } from 'react-router'
import RegistryNames from './../util/RegistryNames'
import RegistryProviderIcons from './../util/RegistryProviderIcons'
import Btn from './../components/Btn'
import Loader from './../components/Loader'
import BtnGroup from './../components/BtnGroup'
import NPECheck from './../util/NPECheck'
import ConvertTimeFriendly from './../util/ConvertTimeFriendly'
import ConvertTimeUTC from './../util/ConvertTimeUTC'
import CopyToClipboard from './../util/CopyToClipboard'
import CreateLocalRepo from './../pages/CreateLocalRepo'
import ControlRoom from './../components/ControlRoom'

export default class Repositories extends Component {
	constructor(props) {
		super(props);
		this.state = {};
	}
	componentDidMount() {
		this.context.actions.listRepos();
	}
	toAddRepo(){
		this.context.router.push('/new-repository');
	}
	renderRepos(){
		let filteredRepos = this.props.repos.sort((repo1, repo2) => repo1.name > repo2.name ? 1 : -1).filter((repo) => {
			if(!this.props.reposFilterQuery) return true;

			return repo.name.indexOf(this.props.reposFilterQuery) > -1
		});

		if(!filteredRepos.length) {
			return this.renderNoResults();
		}

		return (
			<div className="RepoList FlexColumn" key={3}>
				{filteredRepos.map(this.renderRepoItem.bind(this))}
			</div>
		);
	}
	renderRepoItem(repo, index){
		return (
			<Link to={`/repository/${repo.id}`}  key={index}>
			<div className="Flex1 RepoItem FlexColumn">
				<div className="Inside FlexRow">
					<img className="ProviderIcon"
					     src={RegistryProviderIcons(repo.provider)}/>
					<div className="Flex1 FlexColumn">
						<span className="RepoName">{repo.name}</span>
						<span className="RepoProvider">{RegistryNames(true)[repo.provider]}</span>
					</div>
					{this.renderRepoItemDetails(repo)}
					<div className="FlexColumn" style={{flex: '0.45', alignItems: 'flex-end', paddingRight: '7px', justifyContent: 'center'}}>
						<span className="LastWebhookStatus"></span>
					</div>
				</div>
			</div>
			</Link>
		);
	}
	renderRepoItemDetails(repo){
		let lastEvent = repo.lastEvent
		let lastSynced = repo.lastSyncTime;

		if(!repo.local && !lastSynced) {
			return (
				<div className="Flex2 FlexColumn UnknownDetails">
					Retrieving repository details..
				</div>
			);
		}

		if(!lastEvent) {
			return (
				<div className="Flex2 FlexColumn UnknownDetails">
					No events found. {(lastSynced) ? `Last synced at ${ConvertTimeUTC(new Date(lastSynced))}` : null}
				</div>	
			);
		}

		let friendlyTime = (lastEvent.eventTime) ? ConvertTimeFriendly(lastEvent.eventTime) : 'Unknown';
		return (
			<div className="Flex2 FlexColumn JustifyCenter">
				<div className="FlexRow">
					<span className="LastPushed">Pushed image <span className="LightBlueColor">{repo.name}</span></span>
					<span className="Label">&nbsp;&mdash;&nbsp;{friendlyTime}</span>
				</div>
				<div className="FlexRow">
					{lastEvent.imageTags.map((tag, index) => {
						return (
							<span className="Tag" key={index}>{tag}</span>
						);
					})}
				</div>
			</div>
		);
	}
	renderSearchRepos(){
		return (
			<input key={1}
				   className="BlueBorder Search"
			       placeholder="Filter repositories.."
				   onChange={(e) => this.context.actions.filterRepos(e, false)}
			/>
		);
	}
	renderLegend(){
		return (
			<div className="ReposLegend" key={2}>
				<div style={{flex: '0.91'}}>Repository</div>
				<div className="Flex2">Last event</div>
			</div>
		);
	}
	renderRepositories(){
		let reposLength = this.props.repos.length;
		let noun = (reposLength == 1) ? 'Repository' : 'Repositories';

		let content = [
			this.renderSearchRepos(),
			this.renderLegend(),
			this.renderRepos()
		];

		if(NPECheck(this.props, 'addRepo/isCreatingLocalRepo', false)) {
			content = this.renderCreateNewLocalRepo();
		}

		return (
			<div className="ContentContainer">
				<div className="PageHeader">
					<h2>
						{`${reposLength} ${noun}`}
					</h2>
					<div className="FlexRow">
						<div className="Flex1">
							<BtnGroup buttons={[{icon: 'icon icon-dis-add', toolTip: 'Create Local Repository', onClick: () => this.context.actions.toggleCreateNewLocalRepo() }]} />
						</div>
						<div className="Flex1">
							<Link to="/new-repository">
								<BtnGroup buttons={[{icon: 'icon icon-dis-repo', toolTip: 'Add Remote Repository'}]} />
							</Link>
						</div>
					</div>
				</div>
				<div>
					{content}
				</div>
			</div>
		);
	}
	renderNoRepositories(){
		return (
			<div className="ContentContainer">
				<div className="NoRepositories">
					<h3>
						You have no container repositories
					</h3>
					<div className="FlexRow">
						<div className="Flex1" style={{margin: '0 10px'}}>
							<p>
								<strong>Local Repositories</strong>&nbsp;
								 are hosted by Europa and backed by your storage
								 backend. Local Repositories support the Docker V2 API and support the
								 complete range of operations from push and pull to listing tags and
								 repositories.
							</p>
							<Btn className="LargeBlueButton"
								 style={{width:'100%', maxWidth: '100%', fontSize: '1.15rem'}}
							     onClick={() => this.context.actions.toggleCreateNewLocalRepo()}
							 	 text="Add Repository"
							 	 canClick={true} >
							 	 <i className="icon icon-dis-local" />
							 	 Create a Local Repository
							 </Btn>
						</div>
						<div className="Flex1" style={{margin: '0 10px'}}>
							<p>
								<strong>Remote Repositories</strong>&nbsp;
								are hosted in third party registries such as EC2
								Container Registry or Google Container Registry. Europa can scan these
								remote registries and allow you to create pipelines to them and
								mirror remote repositories locally.
							</p>
							<Btn className="LargeBlueButton"
								 style={{width:'100%', maxWidth: '100%', fontSize: '1.15rem'}}
							     onClick={() => this.toAddRepo()}
							 	 canClick={true} >
						 	 	<i className="icon icon-dis-cloud" />
						 	 	Connect a Remote Repository
							 </Btn>
						</div>
					</div>
					<div className="FlexColumn NewRepoCommands">
						<div className="HelperText">or</div>
						<div className="HelperText">Push a Docker image to a local repository</div>
						<div className="HelperText FlexRow">
							<div className="Code">
								<span>$ docker push <span id="copyCommands">{`${this.props.dnsName}/${(this.props.isLoggedIn) ? NPECheck(this.props, 'ctx/username', '') + '/': ''}REPO_NAME[:IMAGE_TAG]`}</span></span>
								<i className="icon icon-dis-copy"
								onClick={() => CopyToClipboard(document.getElementById('copyCommands'))}
								data-tip="Click To Copy"
								data-for="ToolTipTop" />
							</div>
						</div>
					</div>
				</div>
			</div>
		);
	}
	renderCreateNewLocalRepo(){
		return (
			<div style={{marginTop: '14px'}}>
				<ControlRoom renderBodyContent={() => <CreateLocalRepo {...this.props} />} 
							 renderHeaderContent={() => {
							 	return (
									<div className="CR_Header">
										<span className="CR_HeaderTitle">
											New Repository
										</span>
										<span className="CR_HeaderClose">
											<i className="icon-dis-close"
											onClick={ () => this.context.actions.toggleCreateNewLocalRepo() } />
										</span>
									</div>
							 	);
							 }}/>
			</div>
		);
	}
	renderNoResults(){
		return (
			<div className="ContentContainer">
				<div className="NoContent">
					<h3>
					</h3>
					<p>
						No Results
					</p>
				</div>
			</div>
		);
	}
	render() {
		if (this.props.reposXHR) {
			return (
				<div className="PageLoader">
					<Loader />
				</div>
			);
		} else if(this.props.repos.length || NPECheck(this.props, 'addRepo/isCreatingLocalRepo', false)) {
			return this.renderRepositories()
		} else {
			return this.renderNoRepositories();
		}
	}
}

Repositories.childContextTypes = {
    actions: PropTypes.object,
    router: PropTypes.object
};

Repositories.contextTypes = {
    actions: PropTypes.object,
    router: PropTypes.object
};