import React, {Component, PropTypes} from 'react'
import NPECheck from './../util/NPECheck'
import { Link } from 'react-router'
import Btn from './../components/Btn'
import Loader from './../components/Loader'
import BtnGroup from './../components/BtnGroup'
import ControlRoom from './../components/ControlRoom'
import Dropdown from './../components/Dropdown'
import CenteredConfirm from './../components/CenteredConfirm'
import RegistryProviderIcons from './../util/RegistryProviderIcons'
import RegistryNames from './../util/RegistryNames'

export default class PipelineConnectRepository extends Component {
  constructor(props) {
    super(props);
    this.state = {
      repoDropdownOpen: false
    };
  }
  renderConfirm() {
    if (this.props.pipelinesStore.setContainerRepoXHR) {
      return (
        <div className="PageLoader">
          <Loader />
        </div>
      );
    }
    return (
      <CenteredConfirm confirmButtonText="Connect"
                       noMessage={true}
                       confirmButtonStyle={{}}
                       onConfirm={ this.context.actions.setContainerRepo }
                       onCancel={ () => this.context.actions.setPipelinePageSection(null) } />
    );
  }
  onRepoClick() {
    if (this.props.initialConnect) {
      this.context.actions.updateInitialRepoConnect(repo);
    } else {
      this.context.actions.addPipelineComponent(repo);
    }
  }
  renderRepoItem(repo, index) {
    return (
      <div key={index}
           className="ListItem FlexRow"
           onClick={this.onRepoClick}>
        <img src={RegistryProviderIcons(repo.provider)} />
        {repo.name}
      </div>
    );
  }
  render() {
    return (
      <div>
        <div className="CR_Header">
          <span className="CR_HeaderTitle">
            Connect Repository
          </span>
          <span className="CR_HeaderClose">
            <i className="icon-dis-close"
               onClick={ () => this.context.actions.setPipelinePageSection(null) } />
          </span>
        </div>
        <div className="CR_BodyContent">
          <div className="ContentContainer">
            <div className="Flex1">
              <label className="small FlexColumn">
                Docker Image Repository
              </label>
              <Dropdown isOpen={this.state.repoDropdownOpen}
                        toggleOpen={() => this.setState({repoDropdownOpen: !this.state.repoDropdownOpen})}
                        listItems={this.props.repos.filter( repo => repo.provider == "EUROPA"
                                                                    && this.props.pipelineStore.pipeline.containerRepoId != repo.id) }
                        renderItem={(repo, index) => this.renderRepoItem(repo, index)}
                        inputPlaceholder="Docker Image Repository"
                        inputClassName="BlueBorder FullWidth White"
                        inputValue={NPECheck(this.props.pipelineStore, 'initialRepoConnect/name', "")}
                        className="Flex1" />
            </div>
            <div className="Flex1">
              {this.renderConfirm()}
            </div>
          </div>
        </div>
      </div>
    );
  }
}

PipelineConnectRepository.childContextTypes = {
    actions: PropTypes.object,
    router: PropTypes.object
};

PipelineConnectRepository.contextTypes = {
    actions: PropTypes.object,
    router: PropTypes.object
};
