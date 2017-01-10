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
						<span className="RepoProvider">{RegistryNames[repo.provider]}</span>
					</div>
					{this.renderRepoItemDetails(repo)}
					<div className="FlexColumn" style={{flex: '0.45', alignItems: 'flex-end', paddingRight: '7px', justifyContent: 'center'}}>
						<span className="LastWebhookStatus">Success</span>
					</div>
				</div>
			</div>
			</Link>
		);
	}
	renderRepoItemDetails(repo){
		let lastEvent = repo.lastEvent
		if(!lastEvent) {
			return (
				<div className="Flex2 FlexColumn UnknownDetails">
					Retrieving repository details..
				</div>
			);
		}

		let friendlyTime = (lastEvent.eventTime) ? ConvertTimeFriendly(lastEvent.eventTime) : 'Unknown';
		return (
			<div className="Flex2 FlexColumn">
				<div className="FlexRow AlignCenter">
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
				<div style={{flex: '1.105'}}>Repository</div>
				<div className="Flex2">Last event</div>
				<div>Last webhook status</div>
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
							<Link to="/new-repository">
								<BtnGroup buttons={[{icon: 'icon icon-dis-repo', toolTip: 'Add Repository'}]} />
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
				<div className="NoContent">
					<h3>
						You have no repositories to monitor
					</h3>		
					<Btn className="LargeBlueButton"
						 onClick={() => this.toAddRepo()}
						 text="Add Repository"
						 canClick={true} />
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
		if(this.props.reposXHR) {
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