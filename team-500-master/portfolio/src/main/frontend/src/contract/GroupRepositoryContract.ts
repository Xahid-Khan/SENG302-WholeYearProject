import {BaseGroupRepositoryContract} from "./BaseGroupRepositoryContract";

export interface GroupRepositoryContract extends BaseGroupRepositoryContract {
    groupId: number,
    repositoryId:number,
    token:String
}