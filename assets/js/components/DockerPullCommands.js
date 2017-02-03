/*
  @author Sam Heutmaker [samheutmaker@gmail.com]
*/

import React, { Component, PropTypes } from 'react'
import NPECheck from './../util/NPECheck'

export default class DockerPullCommands extends Component {
	constructor(props) {
		super(props);
		this.state = {};
	}
	render(){
		let activeRepo = NPECheck(this.props, 'repoDetails/activeRepo', {});

		return (
			<div className="DockerPullCommands">
				<div className="FlexRow Flex1">
					<i className="icon icon-dis-tag"/>
					{activeRepo.pullCommand}
				</div>
				<div className="FlexRow Flex1">
					<i className="icon icon-dis-blank"/>
					<div className="More">more..</div>
				</div>
			</div>
		);
	}		
}

DockerPullCommands.propTypes = {

};

DockerPullCommands.contextTypes = {
	actions: PropTypes.object
};