//
//  Injection.swift
//  ios
//
//  Created by damien on 01/01/2022.
//  Copyright © 2022 orgName. All rights reserved.
//

import Foundation
import shared

class Injection  {
    private static var singleton = Injection()
    private lazy var tasksRepository = TasksRepository(tasksRemoteDataSource: TasksRemoteDataSource(),
                                                                             tasksLocalDataSource: TasksLocalDataSource(databaseDriverFactory: DatabaseDriverFactory()))
    
    static var instance : Injection {
        return singleton
    }
    
    func provideTasksRepository() -> TasksRepository {
        return tasksRepository
    }
}
