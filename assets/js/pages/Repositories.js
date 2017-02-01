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
import ConvertTimeFriendly from './../util/ConvertTimeFriendly'
import ConvertTimeUTC from './../util/ConvertTimeUTC'

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
	toCreateRepo(){
		this.context.router.push('/create-repository');
	}
	renderRepos(){
		let filteredRepos = this.props.repos.filter((repo) => {
			if(!this.props.reposFilterQuery) return true;

			return JSON.stringify(repo).indexOf(this.props.reposFilterQuery) > -1
		});

		if(!filteredRepos.length) {
			return this.renderNoResults();
		}

		return (
			<div className="RepoList FlexColumn">
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

		if(!lastSynced) {
			return (
				<div className="Flex2 FlexColumn UnknownDetails">
					Retrieving repository details..
				</div>
			);
		}

		if(!lastEvent) {
			return (
				<div className="Flex2 FlexColumn UnknownDetails">
					No events found. Last synced at {ConvertTimeUTC(new Date(lastSynced))}
				</div>	
			);
		}

		let friendlyTime = (lastEvent.eventTime) ? ConvertTimeFriendly(lastEvent.eventTime) : 'Unknown';
		return (
			<div className="Flex2 FlexColumn JustifyCenter">
				<div className="FlexRow">
					<span className="LastPushed">Pushed image <span className="LightBlueColor">{repo.name}</span></span>
					<span className="Label">&nbsp;&ndash;&nbsp;{friendlyTime}</span>
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
			<input className="BlueBorder Search"
			       placeholder="Filter repositories.."
				   onChange={(e) => this.context.actions.filterRepos(e, false)}
			/>
		);
	}
	renderLegend(){
		return (
			<div className="ReposLegend">
				<div style={{flex: '0.91'}}>Repository</div>
				<div className="Flex2">Last event</div>
			</div>
		);
	}
	renderRepositories(){
		let reposLength = this.props.repos.length;
		let noun = (reposLength == 1) ? 'Repository' : 'Repositories';
		return (
			<div className="ContentContainer">
				<div className="PageHeader">
					<h2>
						{`${reposLength} ${noun}`}
					</h2>
					<div className="FlexRow">
						<div className="Flex1">
							<Link to="/create-repository">
								<BtnGroup buttons={[{icon: 'icon icon-dis-add', toolTip: 'Create Local Repository'}]} />
							</Link>
						</div>
						<div className="Flex1">
							<Link to="/new-repository">
								<BtnGroup buttons={[{icon: 'icon icon-dis-repo', toolTip: 'Add Remote Repository', leftMargin: true}]} />
							</Link>
						</div>
					</div>
				</div>
				<div>
					{this.renderSearchRepos()}
					{this.renderLegend()}
					{this.renderRepos()}
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
							<p><strong>Local Repositories</strong> are dolor sit amet, cectetuer adipiscing elit, sed diam nonumy nibh euismod tincidunt ut laoreet dolore magna aliquam erat.</p>
							<Btn className="LargeBlueButton"
								 style={{width:'100%', maxWidth: '100%', fontSize: '1.15rem'}}
							     onClick={() => this.toCreateRepo()}
							 	 text="Add Repository"
							 	 canClick={true} >
							 	 <i className="icon icon-dis-local" />
							 	 Create a Local Repository
							 </Btn>
						</div>
						<div className="Flex1" style={{margin: '0 10px'}}>
							<p><strong>Remote Repositories</strong> are dolor sit amet, cectetuer adipiscing elit, sed diam nonumy nibh euismod tincidunt ut laoreet dolore magna aliquam erat.</p>
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
						<div>or</div>
						<div>Push a Docker image to a local repository</div>
						<p><strong>Command</strong> description dolor sit amet, cectetuer adipiscing elit, sed diam nonumy nibh euismod tincidunt ut laoreet dolore magna aliquam erat.</p>
						<div className="Code">
							 $ docker push {this.props.dnsName}/YOUR_NEW_REPO_NAME[:YOUR_IMAGE_TAG]
						</div>
					</div>
				</div>
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
		} else if(this.props.repos.length) {
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