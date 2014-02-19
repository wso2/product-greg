/*
 * Copyright (c) 2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.registry.checkin;

import org.wso2.carbon.registry.synchronization.SynchronizationConstants;
import org.wso2.carbon.registry.synchronization.message.MessageCode;

public class DefaultMessages {
    
    // the information messages
    public static final String HELP_MSG =
            "usage: checkin-client COMMAND URL -u USERNAME -p PASSWORD [OPTION(S)]\n" +
            "\n" +
            "     The checkin-client is a tool that can be used to synchronize the WSO2\n" +
            "     Governance Registry with a Unix or DOS filesystem. You will need to choose\n" +
            "     one of the three valid commands for COMMAND, along with the username and\n" +
            "     password to connect to the registry as the USERNAME and PASSWORD. There\n" +
            "     are also several options that you can pass in.\n" +
            "\n" +
            "     The URL field would either be a complete registry URL, or a path that\n" +
            "     points to a collection on the repository.\n" +
            "\n" +
            "       - Checkout Example -\n" +
            "\n" +
            "     To checkout the root of a registry running on localhost, use the following\n" +
            "     command (depending on your operating system).\n" +
            "         checkin-client.sh co https://localhost:9443/registry/ -u admin -p admin\n" +
            "         checkin-client.bat co https://localhost:9443/registry/ -u admin -p admin\n" +
            "\n" +
            "     To checkout a path foo under the root of a registry running on localhost,\n" +
            "     use the following command (depending on your operating system).\n" +
            "         checkin-client.sh co https://localhost:9443/registry/foo -u admin -p admin\n" +
            "         checkin-client.bat co https://localhost:9443/registry/foo -u admin -p admin\n" +
            "\n" +
            "Valid commands:\n" +
            "  co [checkout]                : Checkout a url/path. If you are checking out \n" +
            "                                 a local registry just provide the path. If you\n" +
            "                                 are checking out a remote registry provide the\n" +
            "                                 registry url followed by the path.\n" +
            "  ci [checkin]                 : Commit the changes back to the registry, You\n" +
            "                                 should give the url/path of the registry, with\n" +
            "                                 the -f option. if the -f option and url/path\n" +
            "                                 not provided, tool will check-in to the same\n" +
            "                                 location it took the checkout from.\n" +
            "  up [update]                  : Get an update for an already checked out path\n" +
            "  add                          : Add new resource/collection to the version control\n" +
            "  delete                       : Remove a resource/collection from the version control\n" +
            "  propset [pset]               : Adding a property to a resource/collection\n" +
            "  propdelete [pdel]            : Removing a property from resource/collection\n " +
            "  status                       : Status of the changed/added/deleted resources/collections\n" +
            "\n" +
            "Valid options:\n" +
            "  -h                           : For the help screen.\n" +
            "  -l [--location] LOCATION     : The LOCATION is the working directory or path\n" +
            "                                 of file to check-in.\n" +
            "  -f [--filename] DUMP_FILE    : The DUMP_FILE is the file containing the dump\n" +
            "                                 for a check-in operation, or the file to which\n" +
            "                                 the dump must be made for a checkout operation.\n" +
            "  -t [--type] CONNECTION_TYPE  : The registry connection type to be used.\n" +
            "                                 CONNECTION_TYPE is \"atom\" or \"ws\".\n" +
            "  -i [--interactive]           : This option is set to be interactive. It will " +
            "                                 ask for user confirmation before checkin resources." +
            "  --mediatype MEDIATYPE        : This option is valid when adding files to the version" +
            "                                 control. This will set the resource mediatype to given" +
            "                                 value when checkin.";

    public static final String SUCCESS_MSG = "Operation invoked Successfully";

    public static final String CHECK_IN_OPERATION_ABORTED_MSG = "Check-in operation aborted!";

    public static final String ADDED_SUCCESS_MSG = UserInteractor.PARAMETER_PLACE_HOLDER +
            " file(s) added successfully. (Marked as 'A')";

    public static final String TRANSMIT_SUCCESS_MSG = UserInteractor.PARAMETER_PLACE_HOLDER +
            " item(s) transmitted successfully.";
    
    public static final String UPDATED_SUCCESS_MSG = UserInteractor.PARAMETER_PLACE_HOLDER +
            " file(s) updated successfully. (Marked as 'U')";

    public static final String CONFLICTED_FAILURE_MSG = UserInteractor.PARAMETER_PLACE_HOLDER +
            " file(s) found to be conflicting. (Marked as 'C')";

    public static final String DELETED_SUCCESS_MSG = UserInteractor.PARAMETER_PLACE_HOLDER +
            " file(s) found to be deleted. (Marked as 'D')";

    public static final String NOT_DELETED_FINAL_MSG = UserInteractor.PARAMETER_PLACE_HOLDER +
            " file(s) found to be removed in the server, but not deleted locally. (Marked as 'ND')";

    public static final String OVERWRITTEN_FINAL_MSG = UserInteractor.PARAMETER_PLACE_HOLDER +
            " file(s) were overwritten. (Marked as 'OW')";

    public static final String NON_OVERWRITTEN_FINAL_MSG = UserInteractor.PARAMETER_PLACE_HOLDER +
            " file(s) were not overwritten. (Marked as 'N-OW')";

    public static final String NO_FILES_ADDED_MSG = "No files were added.";

    public static final String NO_FILES_UPDATED_MSG = "No files were updated";

    public static final String NO_FILES_CONFLICTED_MSG = "No files were conflicting";
    
    public static final String NO_FILES_DELETED_MSG = "No files were deleted";

    public static final String ADDED_MSG =  "A" + "\t" + UserInteractor.PARAMETER_PLACE_HOLDER;
    public static final String SENT_MSG =  "Sending" + "\t\t" + UserInteractor.PARAMETER_PLACE_HOLDER;
    public static final String DELETED_MSG =  "D" + "\t" + UserInteractor.PARAMETER_PLACE_HOLDER;
    public static final String NOT_DELETED_MSG =  "ND" + "\t" + UserInteractor.PARAMETER_PLACE_HOLDER;
    public static final String CONFLICTED_MSG =  "C" + "\t" + UserInteractor.PARAMETER_PLACE_HOLDER;
    public static final String UPDATED_MSG =  "U" + "\t" + UserInteractor.PARAMETER_PLACE_HOLDER;
    public static final String OVERWRITTEN_MSG = "OW" + "\t" + UserInteractor.PARAMETER_PLACE_HOLDER;
    public static final String NON_OVERWRITTEN_MSG = "N-OW" + "\t" + UserInteractor.PARAMETER_PLACE_HOLDER;

    // the exceptions.
    public static final String NO_OPTIONS_PROVIDED_MSG = "You have not provided any options with the command. " + "" +
             "Provide necessary options as shown in the below list..\n" + HELP_MSG;

    public static final String CO_PATH_MISSING_MSG = "The URL or the path of the registry is missing " +
            "Provide the registry URL or the Path just after the co or " +
            "checkout option.\n" + HELP_MSG;

    public static final String USERNAME_MISSING_MSG = "The username of the registry login is missing, " +
            "Provide the username just after the --user or -u option\n" + HELP_MSG;

    public static final String PASSWORD_MISSING_MSG = "The password of the registry login is missing, " +
            "Provide the password just after the --password or -p option\n" + HELP_MSG;
    
    public static final String WORKING_DIR_MISSING_MSG = "The option value for the location is missing, " +
            "Provide the working directory or file to check-in, just after the --location or -l option. " +
            "If you don't specify a path it will be default to the current directory.\n" + HELP_MSG;

    public static final String REGISTRY_TYPE_MISSING_MSG = "The option value for the type is missing, " +
            "Provide the registry type to be used, just after the --type or -t option. " +
            "If you don't specify a path it will be default to the atom.\n" + HELP_MSG;
    
    public static final String WRONG_WORKING_DIR_MSG = "You should provide a directory as " +
            "the working directory. (You have given a file)" +
            "Aborting the operation.\n" + HELP_MSG;

    public static final String DUMP_FILE_MISSING_MSG = "The option value for the file to input/output the dump " +
            "is missing, Provide a filename, just after the --filename or -f option.\n" + HELP_MSG;

    public static final String OPERATION_NOT_FOUND_MSG = "Unable to find the operation. " +
            "Provide an operation to invoke." + HELP_MSG;

    public static final String USERNAME_NOT_PROVIDED_MSG = "You can't connect with the check-in client anonymously. " +
            "Please provide a valid username";

    public static final String RESTORE_URL_NOT_PROVIDED_MSG = "Provide a URL on where to restore the content";

    public static final String ERROR_IN_RESTORING_MSG =  "Error in restoring the path. " + 
                            "Make sure the registry is up and running Or the username, password is correct! " +
                            "and check the user have the WRITE permission to the path.";

    public static final String FILE_DOES_NOT_EXIST_MSG = "The file doesn't exists.";

    public static final String CHECKOUT_BEFORE_CHECK_IN_MSG = "You should either check-in from a place " +
            "already checkout. Or you should provide the url to check-in with the 'co' command.";
    public static final String CHECK_IN_META_INFO_NOT_FOUND_MSG = "The meta information for the checkout directory not found. " +
            "Check-in from a valid location that earlier have done a checkout";

    public static final String ERROR_IN_DUMPING_NO_RESOURCE_OR_NO_PERMISSION_MSG = "Error in dumping the path. " +
            "There is no resource in the given path or the user don't have the READ permission to the path.";

    public static final String ERROR_IN_DUMPING_AUTHORIZATION_FAILED_MSG ="Error in dumping the path. " +
                "Make sure the path is correct or the given username password is correct.";

    public static final String ERROR_IN_DUMPING_MSG ="Error in dumping the path. " +
                "Make sure the path is correct or the given username password is correct.";

    public static final String DUMPING_NON_COLLECTION_MSG = "Error in dumping the path. " +
            "You should provide a path to a collection to dump. The path you have given is not a collection.";

    public static final String FILE_ALREADY_EXISTS_MSG = "The file we are checking out already exists. " +
                        "Make sure the paths to checkout is empty.";

    public static final String FILE_CREATION_FAILED_MSG = "Problem in creating the given file. " +
                    "Please check whether the filename is not valid in your system, or you have enough disk space " +
                    "The checkout/update will be aborted";

    public static final String PROBLEM_IN_CREATING_CONTENT_MSG = "Problem in writing content to the file.";

    public static final String OUTPUT_FILE_NOT_SUPPORTED_MSG = "Output to a file is not supported for " +
            "'update' operation.";

    public static final String CHECKOUT_BEFORE_UPDATE_MSG = "You should either update from a place" +
            "already checkout. Or you should provide the url to update with the 'up' command.";

    public static final String UPDATE_META_INFO_NOT_FOUND_MSG = "The meta information for the checkout " +
            "directory not found. Update from a valid location that earlier have done a checkout";

    public static final String UPDATE_FROM_DIFFERENT_LOCATION_MSG = "You can't get an 'update' from a " +
            "different registry/path than it is originally check out from.";

    public static final String ERROR_DUMP_PATH_RESOURCE_NOT_EXIST_MSG =  "Error in dumping the path." +
                "Make sure the requested resource exists in the registry.";

    public static final String COLLECTION_AND_RESOURCE_SAME_NAME_MSG = "There is a collection and resource " +
             "having the same name. You can't have a directory and filename with the same name";

    public static final String RESOLVE_CONFLICTS_MSG = "Resolve the conflicts before check-in." +
                        " After resolving conflict please remove the files with " + SynchronizationConstants.MINE_FILE_POSTFIX +
                        " and " + SynchronizationConstants.SERVER_FILE_POSTFIX + " suffixes manually.";

    public static final String ERROR_CREATING_META_FILE_MSG = "Error in creating the meta file.";

    public static final String ERROR_WRITING_TO_META_FILE_MSG = "Error in writing to the meta file.";

    public static final String FILE_TO_READ_IS_NOT_FOUND_MSG = "The file to read is not found.";

    public static final String FILE_LENGTH_IS_TOO_LARGE_MSG = "The file length is too large to read.";

    public static final String ERROR_IN_READING_MSG = "Error in reading the file.";

    public static final String ERROR_IN_COMPLETELY_READING_MSG = "Error in reading the file completely.";

    public static final String ERROR_IN_CLOSING_STREAM_MSG = "Error in closing the stream.";

    public static final String ERROR_IN_READING_META_FILE_MSG = "Error in reading the meta file.";

    public static final String ERROR_IN_READING_META_FILE_STREAM_MSG = "Error in reading the meta file stream.";

    public static final String ERROR_IN_CLOSING_META_FILE_STREAM_MSG = "Error in closing the meta file stream.";

    public static final String ERROR_IN_COPYING_MSG = "Error in copying the file.";

    public static final String MALFORMED_URL_MSG = "Malformed url is given, Aborting the operation.";

    public static final String ERROR_IN_CONNECTING_REGISTRY_MSG = "Error in connecting to remote registry. " +
            "Make sure the registry is up and running";

    public static final String REALM_SERVICE_FAILED_MSG = "Error in initializing the Realm Service. " +
                    "Some databases like h2, derby in file mode, doesn't allow to have multiple connections. " +
                    "In such case, Please make sure no other is connected to the database. " +
                    "If you running the script in separate place, please make sure you have given absolute paths " +
                    "to the file-based database urls.";

    public static final String REGISTRY_SERVICE_FAILED_MSG = "Error in initializing the Registry Service.";

    public static final String USER_REGISTRY_FAILED_MSG = "Error in getting the user registry.";

    public static final String ERROR_ENCODING_RESOURCE_NAME_MSG = "Error in encoding the resource path." ;
    
    public static final String ERROR_DECODING_PATH_MSG = "Error in decoding the file path.";

    public static final String ERROR_IN_DELETING_MSG = "Failed to delete the file/directory " +
            UserInteractor.PARAMETER_PLACE_HOLDER + ". Try to delete it manually and redo the update.";

    public static final String UNSUPPORTED_DUMP_FORMAT_MSG = "Unsupported dump format.";

    public static final String ERROR_IN_CREATING_TEMP_FILE_FOR_DUMP_MSG = "Error in creating the temp file for dump." +
            "Please check your hard disk has enough space left to create a temporary dump file.";

    public static final String ERROR_IN_READING_TEMP_FILE_OF_DUMP_MSG = "Error in reading the temp file of dump.";

    public static final String ERROR_IN_READING_STREAM_OF_TEMP_FILE_OF_DUMP_MSG = "Error in reading the stream of the" +
            "temporary dump xml.";

    public static final String INVALID_DUMP_CREATE_META_FILE_MSG = "Invalid dump to create a meta file.";

    public static final String ERROR_IN_READING_STREAM_TO_CREATE_META_FILE_MSG = "Error in reading stream to" +
            "create meta file.";

    public static final String ERROR_IN_CREATING_XML_STREAM_WRITER_MSG = "Error in creating the xml stream writer.";

    // user input requesting messages
    public static final String CHECK_IN_RESOURCES_CONFIRM_MSG = "When you are 'checking in' resources in the registry " +
            "of the given path will "+
            "be replaced with your local changes.\n" +
            "Are you sure you want to continue? [Y-YES/N-NO] " +
            "(default no) ";


    public static final String DIRECTORY_DELETE_CONFIRM_MSG =  "The collection correspond to the '" +
             UserInteractor.PARAMETER_PLACE_HOLDER + " ' directory is not available in the registry server. " +
            "Do you want to remove the directory and all its content (including " +
            "sub directories) from the local file system?. Note that if you have local changes inside " +
            "the directory, they all will be lost.\n" +
            "[Y-YES/N-NO/A-YES TO ALL/NA-NO TO ALL] (Default no) ";

    public static final String FILE_DELETE_CONFIRM_MSG =  "The file correspond to the '" +
            UserInteractor.PARAMETER_PLACE_HOLDER + "' file is not available in the registry server. " +
            "Do you want to remove the file from the local file system?. " +
            "Note that if you have local changes inside the file they all will be lost. \n" +
            "[Y-YES/N-NO/A-YES TO ALL/NA-NO TO ALL] (Default no) ";

    public static final String CHECKOUT_OLD_VERSION_MSG = "Unsupported carbon version. The check-in client " +
            "supports only carbon versions > 3.0.0";

    public static final String FILE_OVERWRITE_CONFIRM_MSG = "The file '" + UserInteractor.PARAMETER_PLACE_HOLDER +
            "' already exists. Do you want to overwrite it?. \n" +
            "[Y-YES/N-NO/A-YES TO ALL/NA-NO TO ALL] (Default no) ";

    public static final String KEEP_DELETED_FILE_MSG = "The file '" + UserInteractor.PARAMETER_PLACE_HOLDER + "'" +
            " is deleted in your local file system. But still exists on the server. Do you want to keep it deleted?";

    public static String getMessageFromCode(MessageCode messageCode) {
        switch(messageCode) {
            case HELP:
                return HELP_MSG;
            case NO_OPTIONS_PROVIDED:
                return NO_OPTIONS_PROVIDED_MSG;
            case CO_PATH_MISSING:
                return CO_PATH_MISSING_MSG;
            case USERNAME_MISSING:
                return USERNAME_MISSING_MSG;
            case PASSWORD_MISSING:
                return PASSWORD_MISSING_MSG;
            case WORKING_DIR_MISSING:
                return WORKING_DIR_MISSING_MSG;
            case REGISTRY_TYPE_MISSING:
                return REGISTRY_TYPE_MISSING_MSG;
            case WRONG_WORKING_DIR:
                return WRONG_WORKING_DIR_MSG;
            case DUMP_FILE_MISSING:
                return DUMP_FILE_MISSING_MSG;
            case OPERATION_NOT_FOUND:
                return OPERATION_NOT_FOUND_MSG;
            case USERNAME_NOT_PROVIDED:
                return USERNAME_NOT_PROVIDED_MSG;
            case SUCCESS:
                return SUCCESS_MSG;
            case CHECK_IN_RESOURCES_CONFIRMATION:
                return CHECK_IN_RESOURCES_CONFIRM_MSG;
            case CHECK_IN_OPERATION_ABORTED:
                return CHECK_IN_OPERATION_ABORTED_MSG;
            case RESTORE_URL_NOT_PROVIDED:
                return RESTORE_URL_NOT_PROVIDED_MSG;
            case ERROR_IN_RESTORING:
                return ERROR_IN_RESTORING_MSG;
            case FILE_DOES_NOT_EXIST:
                return FILE_DOES_NOT_EXIST_MSG;
            case CHECKOUT_BEFORE_CHECK_IN:
                return CHECKOUT_BEFORE_CHECK_IN_MSG;
            case CHECK_IN_META_INFO_NOT_FOUND:
                return CHECK_IN_META_INFO_NOT_FOUND_MSG;
            case RESOLVE_CONFLICTS:
                return RESOLVE_CONFLICTS_MSG;
            case ERROR_IN_DUMPING_NO_RESOURCE_OR_NO_PERMISSION:
                return ERROR_IN_DUMPING_NO_RESOURCE_OR_NO_PERMISSION_MSG;
            case ERROR_IN_DUMPING_AUTHORIZATION_FAILED:
                return ERROR_IN_DUMPING_AUTHORIZATION_FAILED_MSG;
            case ERROR_IN_DUMPING:
                return ERROR_IN_DUMPING_MSG;
            case DUMPING_NON_COLLECTION:
                return DUMPING_NON_COLLECTION_MSG;
            case ADDED_SUCCESS:
                return ADDED_SUCCESS_MSG;
            case TRANSMIT_SUCCESS:
                return TRANSMIT_SUCCESS_MSG;
            case NO_FILES_ADDED:
                return NO_FILES_ADDED_MSG;
            case FILE_ALREADY_EXISTS:
                return FILE_ALREADY_EXISTS_MSG;
            case FILE_CREATION_FAILED:
                return FILE_CREATION_FAILED_MSG;
            case PROBLEM_IN_CREATING_CONTENT:
                return PROBLEM_IN_CREATING_CONTENT_MSG;
            case ADDED:
                return ADDED_MSG;
            case SENT:
                return SENT_MSG;
            case OUTPUT_FILE_NOT_SUPPORTED:
                return OUTPUT_FILE_NOT_SUPPORTED_MSG;
            case CHECKOUT_BEFORE_UPDATE:
                return CHECKOUT_BEFORE_UPDATE_MSG;
            case UPDATE_META_INFO_NOT_FOUND:
                return UPDATE_META_INFO_NOT_FOUND_MSG;
            case UPDATE_FROM_DIFFERENT_LOCATION:
                return UPDATE_FROM_DIFFERENT_LOCATION_MSG;
            case ERROR_DUMP_PATH_RESOURCE_NOT_EXIST:
                return ERROR_DUMP_PATH_RESOURCE_NOT_EXIST_MSG;
            case UPDATED_SUCCESS:
                return UPDATED_SUCCESS_MSG;
            case CONFLICTED_FAILURE:
                return CONFLICTED_FAILURE_MSG;
            case DELETED_SUCCESS:
                return DELETED_SUCCESS_MSG;
            case NOT_DELETED_FINAL:
                return NOT_DELETED_FINAL_MSG;
            case NO_FILES_UPDATED:
                return NO_FILES_UPDATED_MSG;
            case NO_FILES_CONFLICTED:
                return NO_FILES_CONFLICTED_MSG;
            case NO_FILES_DELETED:
                return NO_FILES_DELETED_MSG;
            case COLLECTION_AND_RESOURCE_SAME_NAME:
                return COLLECTION_AND_RESOURCE_SAME_NAME_MSG;
            case DELETED:
                return DELETED_MSG;
            case NOT_DELETED:
                return NOT_DELETED_MSG;
            case CONFLICTED:
                return CONFLICTED_MSG;
            case UPDATED:
                return UPDATED_MSG;
            case ERROR_CREATING_META_FILE:
                return ERROR_CREATING_META_FILE_MSG;
            case ERROR_WRITING_TO_META_FILE:
                return ERROR_WRITING_TO_META_FILE_MSG;
            case FILE_TO_READ_IS_NOT_FOUND:
                return FILE_TO_READ_IS_NOT_FOUND_MSG;
            case FILE_LENGTH_IS_TOO_LARGE:
                return FILE_LENGTH_IS_TOO_LARGE_MSG;
            case ERROR_IN_READING:
                return ERROR_IN_READING_MSG;
            case ERROR_IN_COMPLETELY_READING:
                return ERROR_IN_COMPLETELY_READING_MSG;
            case ERROR_IN_CLOSING_STREAM:
                return ERROR_IN_CLOSING_STREAM_MSG;
            case ERROR_IN_READING_META_FILE:
                return ERROR_IN_READING_META_FILE_MSG;
            case ERROR_IN_READING_META_FILE_STREAM:
                return ERROR_IN_READING_META_FILE_STREAM_MSG;
            case ERROR_IN_CLOSING_META_FILE_STREAM:
                return ERROR_IN_CLOSING_META_FILE_STREAM_MSG;
            case ERROR_IN_COPYING:
                return ERROR_IN_COPYING_MSG;
            case MALFORMED_URL:
                return MALFORMED_URL_MSG;
            case ERROR_IN_CONNECTING_REGISTRY:
                return ERROR_IN_CONNECTING_REGISTRY_MSG;
            case REALM_SERVICE_FAILED:
                return REALM_SERVICE_FAILED_MSG;
            case REGISTRY_SERVICE_FAILED:
                return REGISTRY_SERVICE_FAILED_MSG;
            case USER_REGISTRY_FAILED:
                return USER_REGISTRY_FAILED_MSG;
            case ERROR_ENCODING_RESOURCE_NAME:
                return ERROR_ENCODING_RESOURCE_NAME_MSG;
            case ERROR_DECODING_PATH:
                return ERROR_DECODING_PATH_MSG;
            case ERROR_IN_DELETING:
                return ERROR_IN_DELETING_MSG;
            case DIRECTORY_DELETE_CONFIRMATION:
                return DIRECTORY_DELETE_CONFIRM_MSG;
            case FILE_DELETE_CONFIRMATION:
                return FILE_DELETE_CONFIRM_MSG;
            case UNSUPPORTED_DUMP_FORMAT:
                return UNSUPPORTED_DUMP_FORMAT_MSG;
            case CHECKOUT_OLD_VERSION:
                return CHECKOUT_OLD_VERSION_MSG;
            case ERROR_IN_CREATING_TEMP_FILE_FOR_DUMP:
                return ERROR_IN_CREATING_TEMP_FILE_FOR_DUMP_MSG;
            case ERROR_IN_READING_TEMP_FILE_OF_DUMP:
                return ERROR_IN_READING_TEMP_FILE_OF_DUMP_MSG;
            case ERROR_IN_READING_STREAM_OF_TEMP_FILE_OF_DUMP:
                return ERROR_IN_READING_STREAM_OF_TEMP_FILE_OF_DUMP_MSG;
            case INVALID_DUMP_CREATE_META_FILE:
                return INVALID_DUMP_CREATE_META_FILE_MSG;
            case ERROR_IN_READING_STREAM_TO_CREATE_META_FILE:
                return ERROR_IN_READING_STREAM_TO_CREATE_META_FILE_MSG;
            case ERROR_IN_CREATING_XML_STREAM_WRITER:
                return ERROR_IN_CREATING_XML_STREAM_WRITER_MSG;
            case FILE_OVERWRITE_CONFIRMATION:
                return FILE_OVERWRITE_CONFIRM_MSG;
            case OVERWRITTEN:
                return OVERWRITTEN_MSG;
            case NON_OVERWRITTEN:
                return NON_OVERWRITTEN_MSG;
            case OVERWRITTEN_FINAL:
                return OVERWRITTEN_FINAL_MSG;
            case NON_OVERWRITTEN_FINAL:
                return NON_OVERWRITTEN_FINAL_MSG;
            case KEEP_DELETED_FILE:
                return KEEP_DELETED_FILE_MSG;
            default:
                return null;
        }
    }
}
