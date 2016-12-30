/*
  @author Sam Heutmaker [samheutmaker@gmail.com]
*/

import React, {Component} from 'react'
import Btn from './../components/Btn'
import Msg from './../components/Msg'
import Loader from './../components/Loader'
import Dropdown from './../components/Dropdown'
import isEmpty from './../util/IsEmpty'
import NPECheck from './../util/NPECheck'

export default class UploadGCEServciceAccount extends Component {
	constructor(props) {
		super(props);
		this.state = {};
	}
	componentWillMount() {
		
	}
	componentWillUnmount() {

	}
	render() {
		return (
			<div className="UploadGCEServiceAccount">
					Some shit
			</div>
		);
	}
}

UploadGCEServciceAccount.childContextTypes = {
	actions: React.PropTypes.object,
    state: React.PropTypes.object,
    router: React.PropTypes.object
};

UploadGCEServciceAccount.contextTypes = {
	actions: React.PropTypes.object,
    state: React.PropTypes.object,
    router: React.PropTypes.object
};
