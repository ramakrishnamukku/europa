import React, {Component} from 'react'
import { Link } from 'react-router'
import RegistryProviderIcons from './../util/RegistryProviderIcons'
import Btn from './../components/Btn'
import Msg from './../components/Msg'
import Loader from './../components/Loader'

export default class Repositories extends Component {
	constructor(props) {
		super(props);
		this.state = {};
	}
	componentDidMount() {
		this.context.actions.listRepos();
	}
	renderRepos(){
		if(this.context.state.reposXHR) {
			return (
				<div className="PageLoader">
					<Loader />
				</div>
			);
		}

		let filteredRepos = this.context.state.repos.filter((repo) => {
			if(!this.context.state.reposFilterQuery) return true;

			return JSON.stringify(repo).indexOf(this.context.state.reposFilterQuery) > -1
		});

		if(!filteredRepos.length) {
			return this.renderNoRepositories();
		}

		return (
			<div className="RepoList FlexColumn">
				{filteredRepos.map(this.renderRepoItem)}
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
						<span className="RepoProvider">{repo.provider}</span>
					</div>
					<div className="Flex2 FlexColumn">
						<span className="Label">Last Event:</span>
						<div className="FlexRow AlignCenter">
							<span className="LastPushed">Pushed image hyper-local with tag: </span>
							<span className="Tag">Latest</span>
						</div>
					</div>
					<div className="FlexColumn">
						<span className="Label">Status of last webhook:</span>
						<span className="LastWebhookStatus">Success</span>
					</div>
				</div>
			</div>
			</Link>
		);
	}
	renderSearchRepos(){
		return (
			<input className="BlueBorder Search" 
			       placeholder="Search"
				   onChange={(e) => this.context.actions.filterRepos(e, false)}
			/>
		);
	}
	renderNoRepositories(){
		return (
			<div className="NoContent">
				<h3>
					No Repositories in saved in Monitor
				</h3>		
			</div>
		);
	}
	render() {
		return (
			<div className="ContentContainer">
				<div className="PageHeader">
					<h2>
						Monitored Repositories
					</h2>
					<div className="FlexRow">
						<div className="Flex1">
							<Link to="/new-repository">
								<Btn text="Add Repository"
									 onClick={ () => {} } />
							</Link>
						</div>
					</div>
				</div>
				<div>
					{this.renderSearchRepos()}
				    {this.renderRepos()}
				</div>
			</div>
		);
	}
}

Repositories.childContextTypes = {
    actions: React.PropTypes.object,
    state: React.PropTypes.object,
    router: React.PropTypes.object
};

Repositories.contextTypes = {
    actions: React.PropTypes.object,
    state: React.PropTypes.object,
    router: React.PropTypes.object
};